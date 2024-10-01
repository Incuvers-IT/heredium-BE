package art.heredium.ncloud.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import art.heredium.ncloud.credential.Credentials;

@Configuration
@EnableConfigurationProperties(value = {Credentials.class})
public class PropertiesConfiguration {}
