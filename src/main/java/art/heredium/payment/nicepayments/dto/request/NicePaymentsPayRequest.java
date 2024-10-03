package art.heredium.payment.nicepayments.dto.request;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NicePaymentsPayRequest {
  @NotNull private Long amount;
}
