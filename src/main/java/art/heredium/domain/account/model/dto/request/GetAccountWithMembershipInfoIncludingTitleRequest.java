package art.heredium.domain.account.model.dto.request;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.membership.entity.PaymentStatus;

@Getter
@Setter
public class GetAccountWithMembershipInfoIncludingTitleRequest {
  private LocalDateTime paymentDateFrom;
  private LocalDateTime paymentDateTo;
  private PaymentStatus paymentStatus;
  private String text;
}
