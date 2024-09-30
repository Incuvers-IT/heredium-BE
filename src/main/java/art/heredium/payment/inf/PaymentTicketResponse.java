package art.heredium.payment.inf;

public interface PaymentTicketResponse {
    Long getAmount();

    String getPaymentKey();

    String getPayMethod();
}
