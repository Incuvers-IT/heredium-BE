package art.heredium.payment.tosspayments.feign.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;

import feign.Logger;

import art.heredium.payment.tosspayments.feign.interceptor.TossPaymentsFeignRequestInterceptor;

@RequiredArgsConstructor
public class TossPaymentsFeignConfig {

  @Bean
  Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }

  @Bean
  public TossPaymentsFeignRequestInterceptor nCloudFeignRequestInterceptor() {
    return new TossPaymentsFeignRequestInterceptor();
  }
}
