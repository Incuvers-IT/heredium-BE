package art.heredium.domain.ticket.model.dto.request;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.ticket.model.TicketOrderInfo;

@Getter
@Setter
public class PostTicketUserValidRequest {
  @NotNull private @Valid TicketOrderInfo ticketOrderInfo;
  private String couponUuid;
}
