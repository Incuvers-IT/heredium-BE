package art.heredium.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.DeletedMembershipException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.config.properties.HerediumProperties;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.entity.CouponUsage;
import art.heredium.domain.coupon.model.dto.response.CouponUsageResponse;
import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.entity.PaymentStatus;
import art.heredium.domain.membership.entity.RegistrationType;
import art.heredium.domain.membership.model.dto.request.MembershipConfirmPaymentRequest;
import art.heredium.domain.membership.model.dto.response.MembershipConfirmPaymentResponse;
import art.heredium.domain.membership.model.dto.response.MembershipRefundResponse;
import art.heredium.domain.membership.repository.MembershipRegistrationRepository;
import art.heredium.domain.post.entity.Post;
import art.heredium.ncloud.bean.HerediumAlimTalk;
import art.heredium.ncloud.type.AlimTalkTemplate;
import art.heredium.payment.dto.PaymentsPayRequest;
import art.heredium.payment.inf.PaymentTicketResponse;
import art.heredium.payment.type.PaymentType;

@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipPaymentService {
  private static final Long DEFAULT_MEMBERSHIP_PERIOD = 364L; // days

  private final CouponUsageService couponUsageService;
  private final MembershipRegistrationRepository membershipRegistrationRepository;
  private final HerediumAlimTalk alimTalk;
  private final HerediumProperties herediumProperties;
  private final DateTimeFormatter MEMBERSHIP_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");

  @Transactional(rollbackFor = Exception.class)
  public MembershipConfirmPaymentResponse confirmPayment(
      @NonNull MembershipConfirmPaymentRequest request) {
    final PaymentsPayRequest payRequest = request.getPayRequest();
    final String orderId = payRequest.getOrderId();
    final MembershipRegistration membershipRegistration =
        this.membershipRegistrationRepository
            .findByPaymentOrderId(orderId)
            .orElseThrow(() -> new ApiException(ErrorCode.PAYMENT_ORDER_ID_NOT_FOUND));

    if (Boolean.TRUE.equals(membershipRegistration.getMembership().getIsDeleted())) {
      throw new DeletedMembershipException(
          "선택하신 멤버십은 더 이상 유효하지 않은 멤버십입니다. 추가 문의 사항이 있으시면 고객센터로 연락해 주십시오.");
    }
    final Membership membership = membershipRegistration.getMembership();
    if (membership != null) {
      final Post post = membership.getPost();
      if (post.getOpenDate() != null && post.getOpenDate().isAfter(LocalDate.now())) {
        throw new ApiException(ErrorCode.REGISTERING_MEMBERSHIP_IS_NOT_AVAILABLE);
      }
    }

    this.updateMembershipRegistrationToSuccess(
        membershipRegistration, payRequest.getPaymentKey(), payRequest.getType());
    this.removePendingMembershipRegistrations(membershipRegistration.getAccount().getId());
    List<CouponUsage> deliveredCoupons = this.deliverCouponsToUser(membershipRegistration);

    PaymentTicketResponse pay =
        (PaymentTicketResponse) payRequest.getType().pay(payRequest, payRequest.getAmount());

    this.sendMembershipRegistrationMessageToAlimTalk(membershipRegistration, deliveredCoupons);

    return new MembershipConfirmPaymentResponse(pay.getPaymentAmount());
  }

  @Transactional(rollbackFor = Exception.class)
  public MembershipRefundResponse refundMembership(final long accountId) {
    final MembershipRegistration membershipRegistration =
        this.membershipRegistrationRepository
            .findByAccountIdAndRegistrationTypeAndPaymentStatusAndExpirationDateAfter(
                accountId,
                RegistrationType.MEMBERSHIP_PACKAGE,
                PaymentStatus.COMPLETED,
                LocalDateTime.now())
            .orElseThrow(
                () ->
                    new ApiException(
                        ErrorCode.MEMBERSHIP_REGISTRATION_NOT_FOUND,
                        String.format(
                            "Active membership registration of accountId %s not found",
                            accountId)));
    this.validateMembershipRegistrationForRefund(membershipRegistration);
    String paymentKey = membershipRegistration.getPaymentKey();
    String paymentOrderId = membershipRegistration.getPaymentOrderId();
    List<CouponUsageResponse> rolledBackCouponUsageResponses = new ArrayList<>();
    List<CouponUsage> rolledBackCoupons =
        this.couponUsageService.rollbackCouponDistribution(membershipRegistration.getId());
    if (!rolledBackCoupons.isEmpty()) {
      rolledBackCouponUsageResponses =
          rolledBackCoupons.stream().map(CouponUsageResponse::new).collect(Collectors.toList());
    }
    membershipRegistration.updatePaymentStatus(PaymentStatus.REFUND);
    this.membershipRegistrationRepository.save(membershipRegistration);
    membershipRegistration.getPaymentType().refund(paymentKey, paymentOrderId);
    return MembershipRefundResponse.builder()
        .paymentKey(paymentKey)
        .paymentOrderId(paymentOrderId)
        .paymentType(membershipRegistration.getPaymentType())
        .rolledBackCoupons(rolledBackCouponUsageResponses)
        .build();
  }

  private void validateMembershipRegistrationForRefund(
      @NonNull final MembershipRegistration membershipRegistration) {
    final PaymentType paymentType = membershipRegistration.getPaymentType();
    if (paymentType == null) {
      throw new ApiException(
          ErrorCode.INVALID_MEMBERSHIP_TO_REFUND,
          String.format(
              "Failed to refund membership: paymentType is null, membershipId: %s",
              membershipRegistration.getId()));
    }
    final String paymentKey = membershipRegistration.getPaymentKey();
    final String paymentOrderId = membershipRegistration.getPaymentOrderId();

    // INICIS and TOSSPAYMENTS require paymentKey
    if ((paymentType == PaymentType.INICIS || paymentType == PaymentType.TOSSPAYMENTS)
        && paymentKey == null) {
      throw new ApiException(
          ErrorCode.INVALID_MEMBERSHIP_TO_REFUND,
          String.format(
              "Failed to refund membership: If paymentType is %s, paymentKey must not be null, membershipId: %s",
              paymentType, membershipRegistration.getId()));
    }

    // NICEPAYMENTS requires both paymentKey and paymentOrderId
    if (paymentType == PaymentType.NICEPAYMENTS && (paymentOrderId == null || paymentKey == null)) {
      throw new ApiException(
          ErrorCode.INVALID_MEMBERSHIP_TO_REFUND,
          String.format(
              "Failed to refund membership: If paymentType is %s, paymentKey and paymentOrderId must not be null, membershipId: %s",
              paymentType, membershipRegistration.getId()));
    }
  }

  private List<CouponUsage> deliverCouponsToUser(
      @NonNull MembershipRegistration membershipRegistration) {
    final Membership membership = membershipRegistration.getMembership();
    if (membership == null)
      throw new ApiException(ErrorCode.MEMBERSHIP_NOT_FOUND); // this case should not happen
    final Account account = membershipRegistration.getAccount();
    final List<Coupon> coupons = membership.getCoupons();
    return this.couponUsageService.distributeMembershipAndCompanyCoupons(account, coupons, false);
  }

  private void updateMembershipRegistrationToSuccess(
      @NonNull MembershipRegistration membershipRegistration,
      @NonNull String paymentKey,
      @NonNull PaymentType paymentType) {
    final LocalDateTime now = LocalDateTime.now();
    membershipRegistration.updateRegistrationDate(now);
    membershipRegistration.updateExpirationDate(
        now.plusDays(
                Optional.ofNullable(membershipRegistration.getMembership())
                    .map(Membership::getPeriod)
                    .orElse(DEFAULT_MEMBERSHIP_PERIOD))
            .toLocalDate()
            .atTime(LocalTime.MAX));
    membershipRegistration.updatePaymentDate(now);
    membershipRegistration.updatePaymentStatus(PaymentStatus.COMPLETED);
    membershipRegistration.updatePaymentKey(paymentKey);
    membershipRegistration.updatePaymentType(paymentType);
    this.membershipRegistrationRepository.save(membershipRegistration);
  }

  private void removePendingMembershipRegistrations(final long accountId) {
    List<Long> pendingMembershipRegistrationIds =
        this.membershipRegistrationRepository
            .findByAccountIdAndPaymentStatus(accountId, PaymentStatus.PENDING)
            .stream()
            .map(MembershipRegistration::getId)
            .collect(Collectors.toList());
    this.membershipRegistrationRepository.deleteAllById(pendingMembershipRegistrationIds);
  }

  private void sendMembershipRegistrationMessageToAlimTalk(
      final MembershipRegistration membershipRegistration, List<CouponUsage> coupons) {
    log.info("Start sendMembershipRegistrationMessageToAlimTalk");
    final Map<String, String> params = new HashMap<>();
    params.put("accountName", membershipRegistration.getAccount().getAccountInfo().getName());
    params.put("membershipName", membershipRegistration.getMembership().getName());
    params.put(
        "startDate", membershipRegistration.getRegistrationDate().format(MEMBERSHIP_DATE_FORMAT));
    params.put(
        "endDate", membershipRegistration.getExpirationDate().format(MEMBERSHIP_DATE_FORMAT));
    params.put("detailCoupons", this.buildCouponDetails(coupons));
    params.put("CSTel", herediumProperties.getTel());
    params.put("CSEmail", herediumProperties.getEmail());
    try {
      this.alimTalk.sendAlimTalkWithoutTitle(
          membershipRegistration.getAccount().getAccountInfo().getPhone(),
          params,
          AlimTalkTemplate.USER_REGISTER_MEMBERSHIP_PACKAGE);
    } catch (Exception e) {
      log.warn(
          "Sending message to AlimTalk failed: {}, message params: {}", e.getMessage(), params);
    }
    log.info("End sendMembershipRegistrationMessageToAlimTalk");
  }

  private String buildCouponDetails(List<CouponUsage> coupons) {
    return coupons.stream()
        .map(
            coupon ->
                String.format(
                    "- %s : %s%n- %s : %s, %s%n- %s : %s",
                    "쿠폰명",
                    coupon.getCoupon().getName(),
                    "할인혜택",
                    coupon.getCoupon().getCouponType().getDesc(),
                    coupon.getCoupon().getDiscountPercent() != 100
                        ? coupon.getCoupon().getDiscountPercent() + "%"
                        : "무료",
                    "사용횟수",
                    Boolean.TRUE.equals(coupon.getCoupon().getIsPermanent())
                        ? "상시할인"
                        : coupon.getCoupon().getNumberOfUses() + "회"))
        .collect(Collectors.joining("\n"));
  }
}
