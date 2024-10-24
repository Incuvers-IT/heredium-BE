package art.heredium.core.util;

import java.util.Base64;

public class Base64Encoder {
  private static final String BASIC_AUTH = "Basic ";

  public static final String encodeAuthorization(String token) {
    return BASIC_AUTH + encode(token);
  }

  private static String encode(String text) {
    return Base64.getEncoder().encodeToString(text.getBytes());
  }
}
