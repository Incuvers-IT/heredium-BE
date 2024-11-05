package art.heredium.core.config.apm;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import co.elastic.apm.attach.ElasticApmAttacher;

@Component
public class ApmConfig {
  private static final String SERVER_URL_KEY = "server_url";
  private static final String SERVICE_NAME_KEY = "service_name";
  private static final String ENVIRONMENT_KEY = "environment";
  private static final String APPLICATION_PACKAGES_KEY = "application_packages";
  private static final String LOG_LEVEL_KEY = "log_level";
  private static final String TRANSACTION_SAMPLE_RATE = "transaction_sample_rate";

  @Bean
  public void inItApmProperties() {
    Map<String, String> apmProperties = new HashMap<>(5);
    apmProperties.put(SERVER_URL_KEY, "http://localhost:8200");
    apmProperties.put(SERVICE_NAME_KEY, "heredium-staging");
    apmProperties.put(ENVIRONMENT_KEY, "apm-heredium");
    apmProperties.put(APPLICATION_PACKAGES_KEY, "art.heredium.*");
    apmProperties.put(LOG_LEVEL_KEY, "INFO");
    apmProperties.put(TRANSACTION_SAMPLE_RATE, "1.0");
    ElasticApmAttacher.attach(apmProperties);
  }
}
