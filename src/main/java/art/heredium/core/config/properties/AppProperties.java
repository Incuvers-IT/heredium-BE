package art.heredium.core.config.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Auth auth = new Auth();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Auth {
        private String refreshTokenName;
        private String adminRefreshTokenName;
        private Duration accessTokenExpiry;
        private Duration refreshTokenMaintain;
        private Duration refreshTokenExpiry;
        private Duration mailTokenExpiry;
    }
}
