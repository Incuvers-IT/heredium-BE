package art.heredium.core.config.properties;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {
  private List<String> allowedOrigins;
  private List<String> allowedMethods;
  private List<String> allowedHeaders;
}
