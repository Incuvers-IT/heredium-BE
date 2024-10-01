package art.heredium.oauth.info.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import art.heredium.domain.account.type.GenderType;
import art.heredium.oauth.info.OAuth2UserInfo;

public class KakaoOAuth2UserInfo extends OAuth2UserInfo {

  public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
    super(attributes);
  }

  @Override
  public String getId() {
    Map<String, Object> kakao_account = (Map<String, Object>) attributes.get("kakao_account");

    if (kakao_account == null || kakao_account.get("ci") == null) {
      return attributes.get("id").toString();
    }

    return (String) kakao_account.get("ci");
  }

  @Override
  public String getName() {
    Map<String, Object> kakao_account = (Map<String, Object>) attributes.get("kakao_account");
    if (kakao_account == null || kakao_account.get("name") == null) {
      return null;
    }

    return (String) kakao_account.get("name");
  }

  @Override
  public String getEmail() {
    Map<String, Object> kakao_account = (Map<String, Object>) attributes.get("kakao_account");

    if (kakao_account == null) {
      return null;
    }

    return (String) kakao_account.get("email");
  }

  @Override
  public String getImageUrl() {
    return null;
  }

  @Override
  public String getPhone() {
    Map<String, Object> kakao_account = (Map<String, Object>) attributes.get("kakao_account");

    if (kakao_account == null || kakao_account.get("phone_number") == null) {
      return null;
    }

    String phoneNumber = (String) kakao_account.get("phone_number");
    return phoneNumber.replace("+82 ", "0").replaceAll("\\-", "");
  }

  @Override
  public GenderType getGender() {
    Map<String, Object> kakao_account = (Map<String, Object>) attributes.get("kakao_account");

    if (kakao_account == null || kakao_account.get("gender") == null) {
      return null;
    }

    String gender = (String) kakao_account.get("gender");
    return gender.equals("male") ? GenderType.MAN : GenderType.WOMAN;
  }

  @Override
  public LocalDate getBirthday() {
    Map<String, Object> kakao_account = (Map<String, Object>) attributes.get("kakao_account");

    if (kakao_account == null) {
      return null;
    }

    if (kakao_account.get("birthyear") == null || kakao_account.get("birthday") == null) {
      return null;
    }

    String birthday =
        String.format("%s-%s", kakao_account.get("birthyear"), kakao_account.get("birthday"));
    return LocalDate.parse(birthday, DateTimeFormatter.ofPattern("yyyy-MMdd"));
  }
}
