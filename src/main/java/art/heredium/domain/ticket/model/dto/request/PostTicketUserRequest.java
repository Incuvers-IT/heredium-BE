package art.heredium.domain.ticket.model.dto.request;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.payment.dto.PaymentsPayRequest;

@Getter
@Setter
public class PostTicketUserRequest {
  @NotNull private @Valid PaymentsPayRequest payRequest;
}
