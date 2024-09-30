package art.heredium.domain.ticket.model.dto.request;

import art.heredium.domain.ticket.model.TicketOrderInfo;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PostTicketHanaBankValidRequest {
    @NotNull
    private @Valid PostTicketHanaBankUserCommonRequest userRequest;
    @NotNull
    private @Valid TicketOrderInfo ticketOrderInfo;
}