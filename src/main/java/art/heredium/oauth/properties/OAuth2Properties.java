package art.heredium.oauth.properties;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import art.heredium.oauth.provider.ClientRegistration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.oauth2")
public class OAuth2Properties {
  public Map<String, ClientRegistration> registration;
}
