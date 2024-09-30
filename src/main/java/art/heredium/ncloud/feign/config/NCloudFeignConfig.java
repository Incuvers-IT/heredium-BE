package art.heredium.ncloud.feign.config;

import art.heredium.ncloud.feign.interceptor.NCloudFeignRequestInterceptor;
import art.heredium.ncloud.credential.Credentials;
import feign.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

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
