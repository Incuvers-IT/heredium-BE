package art.heredium.ncloud.config;

import art.heredium.ncloud.credential.Credentials;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {
        Credentials.class
})
public class PropertiesConfiguration {
}