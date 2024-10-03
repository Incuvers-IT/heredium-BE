package art.heredium.payment.nicepayments.feign.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;

import feign.Logger;

import art.heredium.payment.nicepayments.feign.interceptor.NicePaymentsFeignRequestInterceptor;

@RequiredArgsConstructor
public class NicePaymentsFeignConfig {
  @Bean
  Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }

  @Bean
  public NicePaymentsFeignRequestInterceptor nCloudFeignRequestInterceptor() {
    return new NicePaymentsFeignRequestInterceptor();
  }
}
