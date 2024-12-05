package art.heredium.payment.tosspayments.dto.error;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TossErrorResponse {
  private String code;
  private String message;
}
