package art.heredium.payment.inf;

import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.payment.type.PaymentType;

public interface PaymentService<PaymentRequest> {
  PaymentResponse pay(PaymentRequest dto, Long amount);

  void cancel(Ticket ticket, PaymentRequest dto);

  void refund(Ticket ticket);

  void refund(String paymentKey, String paymentOrderId, PaymentType paymentType);
}
