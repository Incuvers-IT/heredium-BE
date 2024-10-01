package art.heredium.domain.account.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.entity.AccountInfo;
import art.heredium.oauth.provider.OAuth2Provider;

@Getter
@Setter
public class GetUserAccountInfoResponse {
  private String name;
  private String email;
  private OAuth2Provider provider;

  public GetUserAccountInfoResponse(Account entity) {
    AccountInfo accountInfo = entity.getAccountInfo();
    this.name = accountInfo.getName();
    this.email = entity.getEmail();
    this.provider = entity.getProviderType();
  }
}
