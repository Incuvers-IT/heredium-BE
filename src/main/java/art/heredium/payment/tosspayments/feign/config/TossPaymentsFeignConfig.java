package art.heredium.payment.tosspayments.feign.config;

import art.heredium.payment.tosspayments.feign.interceptor.TossPaymentsFeignRequestInterceptor;
import feign.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

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
