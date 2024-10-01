package art.heredium.domain.ticket.model.dto.request;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.payment.tosspayments.dto.request.TossPaymentsPayRequest;

@Getter
@Setter
public class PostTicketHanaBankRequest {
  @NotNull private @Valid TossPaymentsPayRequest payRequest;
}
