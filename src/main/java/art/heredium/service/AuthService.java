package art.heredium.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.Claims;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.config.properties.AppProperties;
import art.heredium.core.jwt.AuthToken;
import art.heredium.core.jwt.AuthTokenProvider;
import art.heredium.core.jwt.JwtRedisUtil;
import art.heredium.core.util.CookieUtil;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.entity.AccountInfo;
import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.account.model.dto.request.PostLoginRequest;
import art.heredium.domain.account.model.dto.response.PostLoginResponse;
import art.heredium.domain.account.repository.AccountRepository;
import art.heredium.domain.account.repository.AdminRepository;
import art.heredium.domain.account.type.AuthType;
import art.heredium.oauth.provider.OAuth2Provider;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AuthService {

  private final AuthTokenProvider authTokenProvider;
  private final AccountRepository accountRepository;
  private final AdminRepository adminRepository;
  private final LoadUserService loadUserService;
  private final AppProperties appProperties;
  private final BCryptPasswordEncoder encoder;
  private final JwtRedisUtil jwtRedisUtil;

  public PostLoginResponse login(
      HttpServletResponse response, PostLoginRequest dto, boolean isAdmin) {
    UserPrincipal userPrincipal;
    if (isAdmin) {
      Admin admin = adminRepository.findByEmailEquals(dto.getEmail());
      if (admin == null) {
        throw new ApiException(ErrorCode.USER_NOT_FOUND);
      }
      userPrincipal = (UserPrincipal) loadUserService.loadAdmin(admin.getId());
    } else {
      Account account =
          accountRepository.findByEmailEqualsAndProviderType(dto.getEmail(), OAuth2Provider.EMAIL);
      if (account == null) {
        throw new ApiException(ErrorCode.USER_NOT_FOUND);
      }
      userPrincipal = (UserPrincipal) loadUserService.loadUser(account.getId());
    }

    String loginFailKey = userPrincipal.getEmail() + isAdmin;
    Integer value = jwtRedisUtil.getData(loginFailKey, Integer.class);
    if (isAdmin) {
      if (value != null && value >= 5) {
        Long loginFailExpire = jwtRedisUtil.getDataExpire(loginFailKey);
        throw new ApiException(ErrorCode.LOGIN_FAIL_OVER, loginFailExpire);
      }
    }

    if (!encoder.matches(dto.getPassword(), userPrincipal.getPassword())) {
      Integer count = value == null ? 1 : value + 1;
      jwtRedisUtil.setDataExpire(loginFailKey, count, 5 * 60);
      throw new ApiException(ErrorCode.PASSWORD_NOT_MATCHED, count);
    }

    Date now = new Date();
    String userId = userPrincipal.getId().toString();
    AuthToken accessToken =
        authTokenProvider.createAuthToken(
            userId,
            userPrincipal.getAuth(),
            new Date(now.getTime() + appProperties.getAuth().getAccessTokenExpiry().toMillis()));
    AuthToken refreshToken =
        authTokenProvider.createAuthToken(
            userId,
            userPrincipal.getAuth(),
            new Date(now.getTime() + appProperties.getAuth().getRefreshTokenExpiry().toMillis()));
    jwtRedisUtil.setDataExpire(
        refreshToken.getToken(),
        userId,
        appProperties.getAuth().getRefreshTokenExpiry().getSeconds());

    int cookieMaxAge = (int) appProperties.getAuth().getRefreshTokenExpiry().toMinutes() * 60;
    String refreshTokenName =
        isAdmin
            ? appProperties.getAuth().getAdminRefreshTokenName()
            : appProperties.getAuth().getRefreshTokenName();
    CookieUtil.addCookie(response, refreshTokenName, refreshToken.getToken(), cookieMaxAge);
    updateLoginDate(isAdmin, userId);
    jwtRedisUtil.deleteData(loginFailKey);
    return new PostLoginResponse(
        accessToken.getToken(),
        userPrincipal.getIsSleeper(),
        userPrincipal.getIsSleeper() ? userPrincipal.getName() : null);
  }

  public String refreshToken(
      HttpServletRequest request, HttpServletResponse response, String accessToken) {
    AuthToken authToken = authTokenProvider.convertAuthToken(accessToken);
    if (accessToken == null || !authToken.validate()) {
      throw new ApiException(ErrorCode.TOKEN_VALID_FAIL, 1);
    }

    Claims claims = authToken.getTokenClaims();
    AuthType accessTokenAuth = authToken.getAuth(claims);
    boolean isAdmin = !accessTokenAuth.equals(AuthType.USER);
    String userId = claims.getSubject();

    String refreshTokenName =
        isAdmin
            ? appProperties.getAuth().getAdminRefreshTokenName()
            : appProperties.getAuth().getRefreshTokenName();
    // refresh token
    String refreshToken =
        CookieUtil.getCookie(request, refreshTokenName).map(Cookie::getValue).orElse((null));
    AuthToken authRefreshToken = authTokenProvider.convertAuthToken(refreshToken);

    if (!authRefreshToken.validate() || authRefreshToken.isExpired()) {
      throw new ApiException(ErrorCode.TOKEN_VALID_FAIL, 2);
    }

    String refreshTokenUserId = jwtRedisUtil.getData(refreshToken);
    if (refreshTokenUserId == null || !refreshTokenUserId.equals(userId)) {
      throw new ApiException(ErrorCode.TOKEN_VALID_FAIL, 3);
    }
    Date now = new Date();
    long validTime = authRefreshToken.getTokenClaims().getExpiration().getTime() - now.getTime();

    AuthToken newAccessToken =
        authTokenProvider.createAuthToken(
            userId,
            accessTokenAuth,
            new Date(now.getTime() + appProperties.getAuth().getAccessTokenExpiry().toMillis()));
    if (validTime > appProperties.getAuth().getRefreshTokenMaintain().toMillis()) {
      return newAccessToken.getToken();
    }
    // refresh 토큰 기간이 3일 이하로 남은 경우, refresh 토큰 갱신
    AuthToken newRefreshToken =
        authTokenProvider.createAuthToken(
            userId,
            accessTokenAuth,
            new Date(now.getTime() + appProperties.getAuth().getRefreshTokenExpiry().toMillis()));
    jwtRedisUtil.setDataExpire(
        newRefreshToken.getToken(),
        userId,
        appProperties.getAuth().getRefreshTokenExpiry().getSeconds());

    int cookieMaxAge = (int) appProperties.getAuth().getRefreshTokenExpiry().toMinutes() * 60;
    CookieUtil.addCookie(response, refreshTokenName, newRefreshToken.getToken(), cookieMaxAge);

    // 로그인 일시 갱신.
    updateLoginDate(isAdmin, userId);
    return newAccessToken.getToken();
  }

  private void updateLoginDate(boolean isAdmin, String userId) {
    if (isAdmin) {
      adminRepository
          .findById(Long.valueOf(userId))
          .ifPresent(admin -> admin.getAdminInfo().updateLastLoginDate());
    } else {
      accountRepository
          .findById(Long.valueOf(userId))
          .ifPresent(
              account -> {
                if (account.getAccountInfo() != null) {
                  account.getAccountInfo().updateLastLoginDate();
                }
              });
    }
  }

  public boolean sleeperRelease(String accessToken) {
    AuthToken authToken = authTokenProvider.convertAuthToken(accessToken);
    if (accessToken == null) {
      throw new ApiException(ErrorCode.TOKEN_VALID_FAIL, 1);
    }
    if (!authToken.validate() || authToken.isExpired()) {
      throw new ApiException(ErrorCode.TOKEN_VALID_FAIL, 2);
    }

    Claims claims = authToken.getTokenClaims();
    AuthType accessTokenAuth = authToken.getAuth(claims);
    boolean isAdmin = !accessTokenAuth.equals(AuthType.USER);
    if (isAdmin) {
      throw new ApiException(ErrorCode.TOKEN_VALID_FAIL, 3);
    }

    String userId = claims.getSubject();
    Account account = accountRepository.findById(Long.valueOf(userId)).orElse(null);
    if (account == null || account.getSleeperInfo() == null) {
      throw new ApiException(ErrorCode.USER_NOT_FOUND);
    }

    account.setAccountInfo(new AccountInfo(account.getSleeperInfo(), account));
    account.setSleeperInfo(null);
    return true;
  }

  public Object getRefreshToken(
      HttpServletRequest request, HttpServletResponse response, String accessToken) {
    Map<String, String> result = new HashMap<>();
    result.put("accessToken", accessToken);
    AuthToken authToken = authTokenProvider.convertAuthToken(accessToken);
    if (accessToken == null || !authToken.validate()) {
      result.put("accessToken Fail", "");
      throw new ApiException(ErrorCode.TOKEN_VALID_FAIL, result.toString());
    }

    Claims claims = authToken.getTokenClaims();
    AuthType accessTokenAuth = authToken.getAuth(claims);
    boolean isAdmin = !accessTokenAuth.equals(AuthType.USER);
    String userId = claims.getSubject();
    result.put("isAdmin", isAdmin + "");
    result.put("userId", userId);

    String refreshTokenName =
        isAdmin
            ? appProperties.getAuth().getAdminRefreshTokenName()
            : appProperties.getAuth().getRefreshTokenName();
    String refreshToken =
        CookieUtil.getCookie(request, refreshTokenName).map(Cookie::getValue).orElse((null));
    result.put("refreshToken", refreshToken);
    AuthToken authRefreshToken = authTokenProvider.convertAuthToken(refreshToken);

    if (!authRefreshToken.validate() || authRefreshToken.isExpired()) {
      result.put("refreshToken Fail", "");
      throw new ApiException(ErrorCode.TOKEN_VALID_FAIL, 2);
    }

    String refreshTokenUserId = jwtRedisUtil.getData(refreshToken);
    result.put("refreshTokenUserId", refreshTokenUserId);
    if (refreshTokenUserId == null || !refreshTokenUserId.equals(userId)) {
      result.put("refreshTokenUserId Fail", "");
      throw new ApiException(ErrorCode.TOKEN_VALID_FAIL, 3);
    }
    return result;
  }
}
