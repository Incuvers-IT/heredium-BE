package art.heredium.domain.ticket.model.dto.request;

import art.heredium.domain.ticket.model.TicketOrderInfo;
import art.heredium.payment.tosspayments.dto.request.TossPaymentsPayRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PostTicketNonUserValidRequest {
    @NotNull
    private @Valid PostTicketNonUserCommonRequest userRequest;
    @NotNull
    private @Valid TicketOrderInfo ticketOrderInfo;
}