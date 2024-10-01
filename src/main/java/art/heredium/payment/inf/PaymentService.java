package art.heredium.payment.inf;

import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.payment.type.PaymentType;

public interface PaymentService<PayValid, TicketRequest> {
  PaymentType getPaymentType(TicketRequest dto);

  PayValid valid(Ticket ticket);

  PaymentTicketResponse pay(TicketRequest dto, Long amount);

  void cancel(Ticket ticket, TicketRequest dto);

  void refund(Ticket ticket);
}
