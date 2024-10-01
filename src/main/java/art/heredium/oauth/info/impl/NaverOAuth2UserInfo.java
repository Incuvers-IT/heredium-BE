package art.heredium.oauth.info.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import art.heredium.domain.account.type.GenderType;
import art.heredium.oauth.info.OAuth2UserInfo;

public class NaverOAuth2UserInfo extends OAuth2UserInfo {

  public NaverOAuth2UserInfo(Map<String, Object> attributes) {
    super(attributes);
  }

  @Override
  public String getId() {
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");

    if (response == null || response.get("id") == null) {
      return null;
    }

    return (String) response.get("id");
  }

  @Override
  public String getName() {
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");

    if (response == null || response.get("name") == null) {
      return null;
    }

    return (String) response.get("name");
  }

  @Override
  public String getEmail() {
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");

    if (response == null || response.get("email") == null) {
      return null;
    }

    return (String) response.get("email");
  }

  @Override
  public String getImageUrl() {
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");

    if (response == null || response.get("profile_image") == null) {
      return null;
    }

    return (String) response.get("profile_image");
  }

  @Override
  public String getPhone() {
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");

    if (response == null || response.get("mobile") == null) {
      return null;
    }

    String mobile = (String) response.get("mobile");
    return mobile.replaceAll("\\-", "");
  }

  @Override
  public GenderType getGender() {
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");

    if (response == null || response.get("gender") == null) {
      return null;
    }

    String gender = (String) response.get("gender");
    return gender.equals("M") ? GenderType.MAN : GenderType.WOMAN;
  }

  @Override
  public LocalDate getBirthday() {
    Map<String, Object> response = (Map<String, Object>) attributes.get("response");

    if (response == null) {
      return null;
    }

    if (response.get("birthyear") == null || response.get("birthday") == null) {
      return null;
    }

    String birthday = String.format("%s-%s", response.get("birthyear"), response.get("birthday"));
    return LocalDate.parse(birthday, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
  }
}
