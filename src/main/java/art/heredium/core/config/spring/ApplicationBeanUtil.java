package art.heredium.core.config.spring;

import org.springframework.context.ApplicationContext;

public class ApplicationBeanUtil {
  public static Object getBean(String beanName) {
    ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
    return applicationContext.getBean(beanName);
  }
}
