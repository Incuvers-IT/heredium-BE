package art.heredium.payment.tosspayments.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import art.heredium.payment.dto.TicketPaymentsPayRequest;
import art.heredium.payment.inf.PaymentTicketRequest;
import art.heredium.payment.type.PaymentType;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TossPaymentsPayRequest implements PaymentTicketRequest {
  @NotBlank private String orderId;
  @NotBlank private String paymentKey;
  @NotNull private Long amount;
  private PaymentType type;

  public static TossPaymentsPayRequest from(TicketPaymentsPayRequest payRequest) {
    return TossPaymentsPayRequest.builder()
        .orderId(payRequest.getOrderId())
        .paymentKey(payRequest.getPaymentKey())
        .amount(payRequest.getAmount())
        .type(payRequest.getType())
        .build();
  }
}
