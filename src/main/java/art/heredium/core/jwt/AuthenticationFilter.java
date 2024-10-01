package art.heredium.core.jwt;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.config.error.entity.ErrorEntity;
import art.heredium.core.config.security.SecurityConfig;
import art.heredium.core.util.HeaderUtil;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.account.type.AuthType;

@Slf4j
@Component
@AllArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

  private final AuthTokenProvider authTokenProvider;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    List<RequestMatcher> matchers = new ArrayList<>();
    for (String pattern : SecurityConfig.permitAll) {
      matchers.add(new AntPathRequestMatcher(pattern));
    }
    RequestMatcher ignoreRequestMatcher = new OrRequestMatcher(matchers);
    return ignoreRequestMatcher.matches(request);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String tokenStr = HeaderUtil.getAccessToken(request);
    if (tokenStr != null) {
      AuthToken token = authTokenProvider.convertAuthToken(tokenStr);
      if (!token.validate()) {
        setUnauthorizedResponse(response, ErrorCode.TOKEN_VALID_FAIL);
        return;
      }
      if (token.isExpired()) {
        setUnauthorizedResponse(response, ErrorCode.UNAUTHORIZED);
        return;
      }
      Claims claims = token.getTokenClaims();
      boolean isAdminUrl = request.getRequestURI().startsWith("/api/admin");
      boolean isUserUrl = request.getRequestURI().startsWith("/api/user");
      boolean isAdminAuth = !token.getAuth(claims).equals(AuthType.USER);
      boolean isUserAuth = token.getAuth(claims).equals(AuthType.USER);
      if (((isAdminUrl && !isAdminAuth) || (isUserUrl && !isUserAuth))) {
        setUnauthorizedResponse(response, ErrorCode.TOKEN_USER_NOT_FOUND);
        return;
      }

      Authentication authentication;
      try {
        authentication = authTokenProvider.getAuthentication(token);
      } catch (ApiException e) {
        setUnauthorizedResponse(response, e.getErrorCode());
        return;
      }
      if (authentication == null) {
        return;
      }
      UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
      if (!principal.isEnabled()) {
        setUnauthorizedResponse(response, ErrorCode.USER_DISABLED);
        return;
      }
      if (principal.getIsSleeper()) {
        setUnauthorizedResponse(response, ErrorCode.USER_SLEEPER);
        return;
      }

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    filterChain.doFilter(request, response);
  }

  public void setUnauthorizedResponse(HttpServletResponse response, ErrorCode errorCode) {
    response.setStatus(errorCode.getStatus().value());
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json;charset=utf-8");

    try {
      PrintWriter out = response.getWriter();
      String error = new Gson().toJson(ErrorEntity.status(errorCode).body().getBody());
      out.println(error);
      out.flush();
    } catch (IOException e) {
      // e.printStackTrace();
    }
  }
}
