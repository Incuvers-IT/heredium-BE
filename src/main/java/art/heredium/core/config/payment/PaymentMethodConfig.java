package art.heredium.core.config.payment;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class PaymentMethodConfig {
  @Value("${feature.payment.tosspayments.enable}")
  private boolean tossPaymentsEnable;

  @Value("${feature.payment.nicepayments.enable}")
  private boolean nicepaymentsEnable;

  @Value("${feature.payment.inicis.enable}")
  private boolean inicisEnable;
}
