package art.heredium.oauth.provider;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientRegistration {
  private String clientId;
  private String keyId;
  private String teamId;
  private String privateKey;
  private String clientSecret;
  private String authorizationGrantType;
  private String redirectUri;
  private Set<String> scope;
  private String authorizationUri;
  private String tokenUri;
  private String userInfoUri;
  private String jwkSetUri;
  private String issuerUri;
  private String userTermsUri;
  private String talkChannels;
}
