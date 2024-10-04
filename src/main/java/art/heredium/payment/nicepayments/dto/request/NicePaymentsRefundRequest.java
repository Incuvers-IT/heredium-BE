package art.heredium.payment.nicepayments.dto.request;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NicePaymentsRefundRequest {
  @NotBlank private String reason;
  @NotBlank private String orderId;
}
