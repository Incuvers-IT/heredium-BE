package art.heredium.payment.tosspayments.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class TossPaymentsRefundRequest {
    @NotBlank
    private String cancelReason;
}