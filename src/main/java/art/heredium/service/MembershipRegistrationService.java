package art.heredium.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.AuthUtil;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.repository.MembershipRegistrationRepository;

import static art.heredium.core.config.error.entity.ErrorCode.ANONYMOUS_USER;

@Service
@RequiredArgsConstructor
public class MembershipRegistrationService {

  private final MembershipRegistrationRepository membershipRegistrationRepository;

  public MembershipRegistration getMembershipRegistrationInfo() {
    final long accountId =
        AuthUtil.getCurrentUserAccountId().orElseThrow(() -> new ApiException(ANONYMOUS_USER));
    return this.membershipRegistrationRepository
        .findByAccountIdAndNotExpired(accountId)
        .orElseThrow(() -> new ApiException(ErrorCode.MEMBERSHIP_REGISTRATION_NOT_FOUND));
  }
}
