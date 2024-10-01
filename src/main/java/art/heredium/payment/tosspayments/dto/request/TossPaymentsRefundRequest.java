package art.heredium.payment.tosspayments.dto.request;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TossPaymentsRefundRequest {
  @NotBlank private String cancelReason;
}
