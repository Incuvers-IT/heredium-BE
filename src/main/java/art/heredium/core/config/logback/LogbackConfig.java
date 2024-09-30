package art.heredium.core.config.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class LogbackConfig {

    private final LogbackProperties properties;

    @Bean
    public JoranConfigurator joranConfigurator() {
        LoggerContext context = null;
        try {
            context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.reset();
            for (Map.Entry<Object, Object> entry : properties.getProperties().entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                context.putProperty(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // context.putProperty("log.config.path", path);
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            InputStream is = new ClassPathResource("logback/logback-spring.xml").getInputStream();
            configurator.setContext(context);
            configurator.doConfigure(is);
            is.close();
            return configurator;
        } catch (JoranException e) {
            e.printStackTrace();
            throw new RuntimeException("로그백 컨픽을 읽는 도중 오류가 발생 했습니다.");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("로그 파일을 읽을 수 없습니다.");
        }
    }
}
