package art.heredium.payment.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.payment.inf.PaymentRequest;
import art.heredium.payment.type.PaymentType;

@Getter
@Setter
public class PaymentsPayRequest implements PaymentRequest {
  @NotBlank private String orderId;
  @NotBlank private String paymentKey;
  @NotNull private Long amount;
  private PaymentType type;
}
