package art.heredium;

import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.querydsl.jpa.impl.JPAQueryFactory;

import art.heredium.domain.account.entity.UserPrincipal;

@EnableJpaAuditing
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableFeignClients
public class HerediumApplication {

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(HerediumApplication.class);
    application.addListeners(new ApplicationPidFileWriter());
    application.run(args);
  }

  @PostConstruct
  public void started() {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    Locale.setDefault(Locale.KOREA);
  }

  @Bean
  public AuditorAware<String> auditorProvider() {
    return () -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null
          || !authentication.isAuthenticated()
          || authentication.getPrincipal().equals("anonymousUser")) {
        return Optional.empty();
      }
      UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
      return Optional.of(principal.getName());
    };
  }

  @Bean
  public JPAQueryFactory jpaQueryFactory(EntityManager em) {
    return new JPAQueryFactory(em);
  }
}
