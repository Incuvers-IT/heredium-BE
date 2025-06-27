package art.heredium.domain.account.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.entity.AccountInfo;
import art.heredium.domain.account.model.dto.AccountMembershipRegistrationInfo;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.oauth.provider.OAuth2Provider;

@Getter
@Setter
public class GetUserAccountInfoResponse {
  private Long id;
  private String name;
  private String email;
  private OAuth2Provider provider;
  private String birthDate;
  private String gender;
  private boolean marketingPending;

  @JsonProperty("account_membership_registration_info")
  private AccountMembershipRegistrationInfo accountMembershipRegistrationInfo;

  public GetUserAccountInfoResponse(Account entity, MembershipRegistration membershipRegistration) {
    AccountInfo accountInfo = entity.getAccountInfo();
    this.id = accountInfo.getId();
    this.birthDate = accountInfo.getBirthDate();
    this.gender = accountInfo.getGender();
    this.marketingPending = accountInfo.getMarketingPending();
    this.name = accountInfo.getName();
    this.email = entity.getEmail();
    this.provider = entity.getProviderType();
    if (membershipRegistration != null) {
      this.accountMembershipRegistrationInfo =
          new AccountMembershipRegistrationInfo(
              membershipRegistration.getId(),
              membershipRegistration.getRegistrationDate(),
              membershipRegistration.getExpirationDate());
    }
  }
}
