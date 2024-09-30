package art.heredium.domain.ticket.model.dto.request;

import art.heredium.payment.tosspayments.dto.request.TossPaymentsPayRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PostTicketNonUserRequest {
    @NotNull
    private @Valid TossPaymentsPayRequest payRequest;
}