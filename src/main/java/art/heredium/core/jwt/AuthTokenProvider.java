package art.heredium.core.jwt;

import java.security.Key;
import java.util.Date;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.account.type.AuthType;
import art.heredium.service.LoadUserService;

public class AuthTokenProvider {

  private final Key key;
  private final LoadUserService loadUserService;

  public AuthTokenProvider(String secret, LoadUserService loadUserService) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
    this.loadUserService = loadUserService;
  }

  public AuthToken createAuthToken(String id, Date expiry) {
    return new AuthToken(id, expiry, key);
  }

  public AuthToken createAuthToken(String id, AuthType role, Date expiry) {
    return new AuthToken(id, role, expiry, key);
  }

  public AuthToken convertAuthToken(String token) {
    return new AuthToken(token, key);
  }

  public Authentication getAuthentication(AuthToken authToken) {
    if (authToken.validate() && !authToken.isExpired()) {
      Claims claims = authToken.getTokenClaims();
      UserPrincipal principal =
          authToken.getAuth(claims).equals(AuthType.USER)
              ? (UserPrincipal) loadUserService.loadUser(Long.valueOf(claims.getSubject()))
              : (UserPrincipal) loadUserService.loadAdmin(Long.valueOf(claims.getSubject()));
      return new UsernamePasswordAuthenticationToken(
          principal, authToken, principal.getAuthorities());
    } else {
      throw new ApiException(ErrorCode.TOKEN_VALID_FAIL, 0);
    }
  }
}
