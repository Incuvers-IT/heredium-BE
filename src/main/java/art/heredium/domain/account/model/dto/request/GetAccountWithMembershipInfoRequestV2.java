package art.heredium.domain.account.model.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.membership.entity.PaymentStatus;

@Getter
@Setter
public class GetAccountWithMembershipInfoRequestV2 {
  private LocalDateTime paymentDateFrom;
  private LocalDateTime paymentDateTo;
  private List<PaymentStatus> paymentStatus;
  private String text;
}
