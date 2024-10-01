package art.heredium.oauth.info;

import java.time.LocalDate;
import java.util.Map;

import lombok.ToString;

import art.heredium.domain.account.type.GenderType;

@ToString
public abstract class OAuth2UserInfo {
  protected Map<String, Object> attributes;

  public OAuth2UserInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public abstract String getId();

  public abstract String getName();

  public abstract String getEmail();

  public abstract String getImageUrl();

  public abstract String getPhone();

  public abstract GenderType getGender();

  public abstract LocalDate getBirthday();
}
