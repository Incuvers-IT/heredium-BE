package art.heredium.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.entity.PaymentStatus;
import art.heredium.domain.membership.model.dto.request.MembershipConfirmPaymentRequest;
import art.heredium.domain.membership.model.dto.response.MembershipConfirmPaymentResponse;
import art.heredium.domain.membership.repository.MembershipRegistrationRepository;
import art.heredium.payment.dto.PaymentsPayRequest;
import art.heredium.payment.inf.PaymentTicketResponse;

@Service
@RequiredArgsConstructor
public class MembershipPaymentService {
  private static final Long DEFAULT_MEMBERSHIP_PERIOD = 364L; // days

  private final CouponUsageService couponUsageService;
  private final MembershipRegistrationRepository membershipRegistrationRepository;

  @Transactional(rollbackFor = Exception.class)
  public MembershipConfirmPaymentResponse confirmPayment(
      @NonNull MembershipConfirmPaymentRequest request) {
    final PaymentsPayRequest payRequest = request.getPayRequest();
    final String orderId = payRequest.getOrderId();
    final Optional<MembershipRegistration> membershipRegistration =
        this.membershipRegistrationRepository.findByPaymentOrderId(orderId);
    if (!membershipRegistration.isPresent())
      throw new ApiException(ErrorCode.PAYMENT_ORDER_ID_NOT_FOUND);

    this.updateMembershipRegistrationToSuccess(membershipRegistration.get());
    this.updatePendingMembershipRegistrationsToIgnore(
        membershipRegistration.get().getAccount().getId());
    this.deliverCouponsToUser(membershipRegistration.get());

    PaymentTicketResponse pay =
        (PaymentTicketResponse) payRequest.getType().pay(payRequest, payRequest.getAmount());
    return new MembershipConfirmPaymentResponse(pay.getPaymentAmount());
  }

  private void deliverCouponsToUser(@NonNull MembershipRegistration membershipRegistration) {
    final Membership membership = membershipRegistration.getMembership();
    if (membership == null)
      throw new ApiException(ErrorCode.MEMBERSHIP_NOT_FOUND); // this case should not happen
    final Account account = membershipRegistration.getAccount();
    final List<Coupon> coupons = membership.getCoupons();
    this.couponUsageService.distributeMembershipAndCompanyCoupons(account, coupons);
  }

  private void updateMembershipRegistrationToSuccess(
      @NonNull MembershipRegistration membershipRegistration) {
    final LocalDate now = LocalDate.now();
    membershipRegistration.updateRegistrationDate(now);
    membershipRegistration.updateExpirationDate(
        now.plusDays(
            Optional.ofNullable(membershipRegistration.getMembership())
                .map(Membership::getPeriod)
                .orElse(DEFAULT_MEMBERSHIP_PERIOD)));
    membershipRegistration.updatePaymentDate(now);
    membershipRegistration.updatePaymentStatus(PaymentStatus.COMPLETED);
    this.membershipRegistrationRepository.save(membershipRegistration);
  }

  private void updatePendingMembershipRegistrationsToIgnore(final long accountId) {
    List<MembershipRegistration> pendingMembershipRegistrations =
        this.membershipRegistrationRepository.findByAccountIdAndPaymentStatus(
            accountId, PaymentStatus.PENDING);
    pendingMembershipRegistrations.forEach(
        membershipRegistration ->
            membershipRegistration.updatePaymentStatus(PaymentStatus.IGNORED));
    this.membershipRegistrationRepository.saveAll(pendingMembershipRegistrations);
  }
}
