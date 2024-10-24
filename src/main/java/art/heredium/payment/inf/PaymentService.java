package art.heredium.payment.inf;

import art.heredium.domain.ticket.entity.Ticket;

public interface PaymentService<PaymentRequest> {
  PaymentResponse pay(PaymentRequest dto, Long amount);

  void cancel(Ticket ticket, PaymentRequest dto);

  void refund(Ticket ticket);
}
