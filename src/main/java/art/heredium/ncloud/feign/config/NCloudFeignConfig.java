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
    //    return new NCloudFeignRequestInterceptor(credentials);
    // For testing
    return new NCloudFeignRequestInterceptor(
        new Credentials("01650D4D12B909D827DB", "E06B210F907A7608C55154348C30055E46E0D964"));
  }
}
