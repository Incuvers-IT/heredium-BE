package art.heredium.core.util;

import java.util.Base64;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.util.SerializationUtils;

public class CookieUtil {

  public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
    Cookie[] cookies = request.getCookies();

    if (cookies != null && cookies.length > 0) {
      for (Cookie cookie : cookies) {
        if (name.equals(cookie.getName())) {
          return Optional.of(cookie);
        }
      }
    }
    return Optional.empty();
  }

  public static void addCookie(
      HttpServletResponse response, String name, String value, int maxAge) {
    /*Cookie cookie = new Cookie(name, value);
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(maxAge);
    response.addCookie(cookie);*/

    if (Constants.PROFILE_ACTIVE.equals("prod")) {
      response.addHeader(
          HttpHeaders.SET_COOKIE,
          ResponseCookie.from(name, value)
              .maxAge(maxAge)
              .httpOnly(true)
              .secure(true)
              .path("/")
              .build()
              .toString());
    } else if (Constants.PROFILE_ACTIVE.equals("stage")) {
      response.addHeader(
          HttpHeaders.SET_COOKIE,
          ResponseCookie.from(name, value)
              .maxAge(maxAge)
              .httpOnly(true)
              .sameSite("None")
              .secure(true)
              .path("/")
              .build()
              .toString());
    } else {
      response.addHeader(
          HttpHeaders.SET_COOKIE,
          ResponseCookie.from(name, value).maxAge(maxAge).path("/").build().toString());
    }
  }

  public static void deleteCookie(
      HttpServletRequest request, HttpServletResponse response, String name) {
    Cookie[] cookies = request.getCookies();

    if (cookies != null && cookies.length > 0) {
      for (Cookie cookie : cookies) {
        if (name.equals(cookie.getName())) {
          cookie.setValue("");
          cookie.setPath("/");
          cookie.setMaxAge(0);
          response.addCookie(cookie);
        }
      }
    }
  }

  public static String serialize(Object obj) {
    return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(obj));
  }

  public static <T> T deserialize(Cookie cookie, Class<T> cls) {
    return cls.cast(
        SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie.getValue())));
  }
}
