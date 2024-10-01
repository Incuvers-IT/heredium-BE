package art.heredium.core.config.properties;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "heredium")
public class HerediumProperties {
  private String domain;
  private String tel;
  private String email;
}
