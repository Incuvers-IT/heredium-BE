package art.heredium.oauth.properties;

import art.heredium.oauth.provider.ClientRegistration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.oauth2")
public class OAuth2Properties {
    public Map<String, ClientRegistration> registration;
}
