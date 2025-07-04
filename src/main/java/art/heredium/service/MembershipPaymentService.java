package art.heredium.service;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.config.properties.HerediumProperties;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.entity.CouponUsage;
import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.entity.PaymentStatus;
import art.heredium.domain.membership.repository.MembershipRegistrationRepository;
import art.heredium.ncloud.bean.HerediumAlimTalk;
import art.heredium.ncloud.type.AlimTalkTemplate;
import art.heredium.payment.type.PaymentType;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

//  @Transactional(rollbackFor = Exception.class)
//  public MembershipConfirmPaymentResponse confirmPayment(
//      @NonNull MembershipConfirmPaymentRequest request) {
//    final PaymentsPayRequest payRequest = request.getPayRequest();
//    final String orderId = payRequest.getOrderId();
//    final MembershipRegistration membershipRegistration =
//        this.membershipRegistrationRepository
//            .findByPaymentOrderId(orderId)
//            .orElseThrow(() -> new ApiException(ErrorCode.PAYMENT_ORDER_ID_NOT_FOUND));
//
//    if (Boolean.TRUE.equals(membershipRegistration.getMembership().getIsDeleted())) {
//      throw new DeletedMembershipException(
//          "선택하신 멤버십은 더 이상 유효하지 않은 멤버십입니다. 추가 문의 사항이 있으시면 고객센터로 연락해 주십시오.");
//    }
//    final Membership membership = membershipRegistration.getMembership();
//    if (membership != null) {
//      final Post post = membership.getPost();
//      if (post.getOpenDate() != null && post.getOpenDate().isAfter(LocalDate.now())) {
//        throw new ApiException(ErrorCode.REGISTERING_MEMBERSHIP_IS_NOT_AVAILABLE);
//      }
//    }
//
//    this.updateMembershipRegistrationToSuccess(
//        membershipRegistration, payRequest.getPaymentKey(), payRequest.getType());
//    this.removePendingMembershipRegistrations(membershipRegistration.getAccount().getId());
//    List<CouponUsage> deliveredCoupons = this.deliverCouponsToUser(membershipRegistration);
//
//    PaymentTicketResponse pay =
//        (PaymentTicketResponse) payRequest.getType().pay(payRequest, payRequest.getAmount());
//
//    this.sendMembershipRegistrationMessageToAlimTalk(membershipRegistration, deliveredCoupons);
//
//    return new MembershipConfirmPaymentResponse(pay.getPaymentAmount());
//  }

  private List<CouponUsage> deliverCouponsToUser(
      @NonNull MembershipRegistration membershipRegistration) {
    final Membership membership = membershipRegistration.getMembership();
    if (membership == null)
      throw new ApiException(ErrorCode.MEMBERSHIP_NOT_FOUND); // this case should not happen
    final Account account = membershipRegistration.getAccount();
    final List<Coupon> coupons = membership.getCoupons();
    return this.couponUsageService.distributeMembershipAndCompanyCoupons(account, coupons, false);
  }

//  private void updateMembershipRegistrationToSuccess(
//      @NonNull MembershipRegistration membershipRegistration,
//      @NonNull String paymentKey,
//      @NonNull PaymentType paymentType) {
//    final LocalDateTime now = LocalDateTime.now();
//    membershipRegistration.updateRegistrationDate(now);

    // TODO(MEMBERSHIP) : 만료일 설정
//    membershipRegistration.updateExpirationDate(
//        now.plusDays(
//                Optional.ofNullable(membershipRegistration.getMembership())
//                    .map(Membership::getPeriod)
//                    .orElse(DEFAULT_MEMBERSHIP_PERIOD))
//            .toLocalDate()
//            .atTime(LocalTime.MAX));
//    membershipRegistration.updatePaymentStatus(PaymentStatus.COMPLETED);
//    this.membershipRegistrationRepository.save(membershipRegistration);
//  }

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
    log.info(
        "Start sendMembershipRegistrationMessageToAlimTalk {}, {}",
        membershipRegistration,
        coupons);
    try {
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
      this.alimTalk.sendAlimTalkWithoutTitle(
          membershipRegistration.getAccount().getAccountInfo().getPhone(),
          params,
          AlimTalkTemplate.USER_REGISTER_MEMBERSHIP_PACKAGE);
    } catch (Exception e) {
      log.warn("Sending message to AlimTalk failed: {}", e.getMessage());
    }
    log.info("End sendMembershipRegistrationMessageToAlimTalk");
  }

  private String buildCouponDetails(List<CouponUsage> coupons) {
    // Group couponUsages by couponId to remove redundancy
    final Map<Long, List<CouponUsage>> couponUsagesGroupedByCouponId =
        coupons.stream()
            .collect(Collectors.groupingBy(couponUsage -> couponUsage.getCoupon().getId()));
    return couponUsagesGroupedByCouponId.values().stream()
        .map(
            couponGroup -> {
              // Only get the first coupon from each group
              final CouponUsage coupon = couponGroup.get(0);
              return String.format(
                  "- %s : %s%n  %s : %s, %s%n  %s : %s",
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
                      : coupon.getCoupon().getNumberOfUses() + "회");
            })
        .collect(Collectors.joining("\n"));
  }
}
