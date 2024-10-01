package art.heredium.oauth.info.impl;

import java.time.LocalDate;
import java.util.Map;

import art.heredium.domain.account.type.GenderType;
import art.heredium.oauth.info.OAuth2UserInfo;

public class AppleOAuth2UserInfo extends OAuth2UserInfo {

  public AppleOAuth2UserInfo(Map<String, Object> attributes) {
    super(attributes);
  }

  @Override
  public String getId() {
    return (String) attributes.get("sub");
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getEmail() {
    return (String) attributes.get("email");
  }

  @Override
  public String getImageUrl() {
    return null;
  }

  @Override
  public String getPhone() {
    return null;
  }

  @Override
  public GenderType getGender() {
    return null;
  }

  @Override
  public LocalDate getBirthday() {
    return null;
  }
}
