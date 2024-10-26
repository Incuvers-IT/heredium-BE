package art.heredium.domain.membership.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MembershipConfirmPaymentResponse {
  private long paidAmount;
}
