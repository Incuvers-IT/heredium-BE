package art.heredium.payment.nicepayments.dto.request;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NicePaymentsRefundRequest {
  @NotBlank private String reason;
  @NotBlank private String orderId;

  public static NicePaymentsRefundRequest from(String orderId) {
    return NicePaymentsRefundRequest.builder().reason("환불").orderId(orderId).build();
  }
}
