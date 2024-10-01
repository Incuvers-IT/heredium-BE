package art.heredium.domain.ticket.model.dto.request;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.ticket.model.TicketOrderInfo;

@Getter
@Setter
public class PostTicketHanaBankValidRequest {
  @NotNull private @Valid PostTicketHanaBankUserCommonRequest userRequest;
  @NotNull private @Valid TicketOrderInfo ticketOrderInfo;
}
