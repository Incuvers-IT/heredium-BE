package art.heredium.payment.tosspayments.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import art.heredium.payment.dto.PaymentsPayRequest;
import art.heredium.payment.inf.PaymentRequest;
import art.heredium.payment.type.PaymentType;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TossPaymentsPayRequest implements PaymentRequest {
  @NotBlank private String orderId;
  @NotBlank private String paymentKey;
  @NotNull private Long amount;
  private PaymentType type;

  public static TossPaymentsPayRequest from(PaymentsPayRequest payRequest) {
    return TossPaymentsPayRequest.builder()
        .orderId(payRequest.getOrderId())
        .paymentKey(payRequest.getPaymentKey())
        .amount(payRequest.getAmount())
        .type(payRequest.getType())
        .build();
  }
}
