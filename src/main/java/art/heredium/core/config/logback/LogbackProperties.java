package art.heredium.core.config.logback;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.util.Properties;

@Configuration
@Getter
public class LogbackProperties {

    private final Environment environment;

    private Properties properties;

    public LogbackProperties(Environment environment) throws IOException {
        this.environment = environment;

        ResourceLoader resourceLoader = new DefaultResourceLoader(getClass().getClassLoader());
        Resource resource = resourceLoader.getResource(String.format("classpath:/logback/logback-%s.properties", environment.getActiveProfiles()[0]));
        properties = new Properties();
        properties.load(resource.getInputStream());
    }
}
