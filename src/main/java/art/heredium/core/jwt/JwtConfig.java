package art.heredium.core.jwt;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import art.heredium.service.LoadUserService;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {
  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.mail-secret}")
  private String mailSecret;

  private final LoadUserService loadUserService;

  @Bean
  public AuthTokenProvider jwtProvider() {
    return new AuthTokenProvider(secret, loadUserService);
  }

  @Bean
  public MailTokenProvider jwtMailProvider() {
    return new MailTokenProvider(mailSecret);
  }
}
