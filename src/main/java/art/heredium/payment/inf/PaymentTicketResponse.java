package art.heredium.payment.inf;

public interface PaymentTicketResponse extends PaymentResponse {
  Long getPaymentAmount();

  String getPaymentKey();

  String getPayMethod();
}
