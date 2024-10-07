package art.heredium.payment.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.payment.inf.PaymentTicketRequest;
import art.heredium.payment.type.PaymentType;

@Getter
@Setter
public class TicketPaymentsPayRequest implements PaymentTicketRequest {
  @NotBlank private String orderId;
  @NotBlank private String paymentKey;
  @NotNull private Long amount;
  private PaymentType type;
}
