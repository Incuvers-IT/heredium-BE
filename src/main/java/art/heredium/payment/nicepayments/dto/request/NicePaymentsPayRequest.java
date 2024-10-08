package art.heredium.payment.nicepayments.dto.request;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import art.heredium.payment.dto.PaymentsPayRequest;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NicePaymentsPayRequest {
  @NotNull private Long amount;

  public static NicePaymentsPayRequest from(PaymentsPayRequest payRequest) {
    return NicePaymentsPayRequest.builder().amount(payRequest.getAmount()).build();
  }
}
