package art.heredium.domain.account.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Account;
import art.heredium.oauth.provider.OAuth2Provider;

@Getter
@Setter
public class GetAuthFindIdResponse {
  private String email;
  private OAuth2Provider provider;

  public GetAuthFindIdResponse(Account account) {
    this.email = Constants.emailMasking(account.getEmail());
    this.provider = account.getProviderType();
  }
}
