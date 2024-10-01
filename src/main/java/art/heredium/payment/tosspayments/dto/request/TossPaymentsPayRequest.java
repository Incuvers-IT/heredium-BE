package art.heredium.payment.tosspayments.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.payment.inf.PaymentTicketRequest;
import art.heredium.payment.type.PaymentType;

@Getter
@Setter
public class TossPaymentsPayRequest implements PaymentTicketRequest {
  @NotBlank private String orderId;
  @NotBlank private String paymentKey;
  @NotNull private Long amount;
  private PaymentType type;
}
