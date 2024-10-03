package art.heredium.service;

import java.time.LocalDate;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.AuthUtil;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.repository.AccountRepository;
import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.repository.MembershipRegistrationRepository;
import art.heredium.domain.membership.repository.MembershipRepository;

import static art.heredium.core.config.error.entity.ErrorCode.ANONYMOUS_USER;

@Service
@RequiredArgsConstructor
public class MembershipRegistrationService {

  private final MembershipRegistrationRepository membershipRegistrationRepository;
  private final MembershipRepository membershipRepository;
  private final AccountRepository accountRepository;
  private final CouponUsageService couponUsageService;

  public MembershipRegistration getMembershipRegistrationInfo() {
    final long accountId =
        AuthUtil.getCurrentUserAccountId().orElseThrow(() -> new ApiException(ANONYMOUS_USER));
    return this.membershipRegistrationRepository
        .findByAccountIdAndNotExpired(accountId)
        .orElseThrow(() -> new ApiException(ErrorCode.MEMBERSHIP_REGISTRATION_NOT_FOUND));
  }

  @Transactional(rollbackFor = Exception.class)
  public long registerMembership(long membershipId) {
    final long accountId =
        AuthUtil.getCurrentUserAccountId()
            .orElseThrow(() -> new ApiException(ErrorCode.ANONYMOUS_USER));
    final Optional<MembershipRegistration> existingMembershipRegistration =
        this.membershipRegistrationRepository.findByAccountIdAndNotExpired(accountId);
    if (existingMembershipRegistration.isPresent()) {
      throw new ApiException(ErrorCode.MEMBERSHIP_REGISTRATION_ALREADY_EXISTS);
    }
    final Membership membership =
        this.membershipRepository
            .findByIdAndIsEnabledTrue(membershipId)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMBERSHIP_NOT_FOUND));
    final Account account =
        this.accountRepository
            .findById(accountId)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    // TODO: IH-9 Implement payment
    final LocalDate registrationDate = LocalDate.now();
    final LocalDate expirationDate = registrationDate.plusMonths(membership.getPeriod());
    final MembershipRegistration membershipRegistration =
        this.membershipRegistrationRepository.save(
            new MembershipRegistration(account, membership, registrationDate, expirationDate));
    this.couponUsageService.distributeCoupons(account, membership.getCoupons());
    // TODO: IH-6 Send notification
    return membershipRegistration.getId();
  }
}
