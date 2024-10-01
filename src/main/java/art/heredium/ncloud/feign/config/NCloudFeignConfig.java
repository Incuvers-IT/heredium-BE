package art.heredium.ncloud.feign.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;

import feign.Logger;

import art.heredium.ncloud.credential.Credentials;
import art.heredium.ncloud.feign.interceptor.NCloudFeignRequestInterceptor;

@RequiredArgsConstructor
public class NCloudFeignConfig {

  private final Credentials credentials;

  @Bean
  Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }

  @Bean
  public NCloudFeignRequestInterceptor nCloudFeignRequestInterceptor() {
    return new NCloudFeignRequestInterceptor(credentials);
  }
}
