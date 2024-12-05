package art.heredium.core.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Util {
  private static final String BASIC_AUTH = "Basic ";

  public static final String encodeAuthorization(String token) {
    return BASIC_AUTH + encode(token);
  }

  private static String encode(String text) {
    return Base64.getEncoder().encodeToString(text.getBytes());
  }

  public static String decode(String text) {
    return new String(Base64.getDecoder().decode(text), StandardCharsets.UTF_8);
  }
}
