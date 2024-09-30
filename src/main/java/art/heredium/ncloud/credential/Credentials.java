package art.heredium.ncloud.credential;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties("ncloud.credentials")
@ConstructorBinding
public class Credentials {
    private final String accessKey;
    private final String secretKey;
}
