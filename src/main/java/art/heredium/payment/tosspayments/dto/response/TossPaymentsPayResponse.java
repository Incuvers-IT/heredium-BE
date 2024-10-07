package art.heredium.payment.tosspayments.dto.response;

import lombok.Getter;
import lombok.Setter;

import art.heredium.payment.inf.PaymentTicketResponse;

@Getter
@Setter
public class TossPaymentsPayResponse implements PaymentTicketResponse {

  private String mId;
  private String version;
  private String lastTransactionKey;
  private String paymentKey;
  private String orderId;
  private String orderName;
  private String currency;
  private String method;
  private String status;
  private String requestedAt;
  private String approvedAt;
  private boolean useEscrow;
  private boolean cultureExpense;
  private Card card;
  private Receipt receipt;
  private Checkout checkout;
  private String type;
  private String country;
  private int totalAmount;
  private int balanceAmount;
  private int suppliedAmount;
  private int vat;
  private int taxFreeAmount;
  private int taxExemptionAmount;

  @Override
  public Long getPaymentAmount() {
    return (long) totalAmount;
  }

  @Override
  public String getPayMethod() {
    return method;
  }

  @Getter
  @Setter
  public static class Checkout {
    private String url;
  }

  @Getter
  @Setter
  public static class Receipt {
    private String url;
  }

  @Getter
  @Setter
  public static class Card {
    private String acquireStatus;
    private String ownerType;
    private String cardType;
    private boolean useCardPoint;
    private String approveNo;
    private boolean isInterestFree;
    private int installmentPlanMonths;
    private String number;
    private String acquirerCode;
    private String issuerCode;
    private int amount;
  }
}
/*
{
        "mId": "tosspayments",
        "version": "2022-11-16",
        "lastTransactionKey": "B7103F204998813B889C77C043D09502",
        "paymentKey": "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6",
        "orderId": "a4CWyWY5m89PNh7xJwhk1",
        "orderName": "토스 티셔츠 외 2건",
        "currency": "KRW",
        "method": "카드",
        "status": "DONE",
        "requestedAt": "2021-01-01T10:01:30+09:00",
        "approvedAt": "2021-01-01T10:05:40+09:00",
        "useEscrow": false,
        "cultureExpense": false,
        "card": {
        "amount": 15000,
        "issuerCode": "61",
        "acquirerCode": "31",
        "number": "12341234****123*",
        "installmentPlanMonths": 0,
        "isInterestFree": false,
        "interestPayer": null,
        "approveNo": "00000000",
        "useCardPoint": false,
        "cardType": "신용",
        "ownerType": "개인",
        "acquireStatus": "READY"
        },
        "virtualAccount": null,
        "transfer": null,
        "mobilePhone": null,
        "giftCertificate": null,
        "foreignEasyPay": null,
        "cashReceipt": null,
        "receipt": {
        "url": "https://merchants.tosspayments.com/web/serve/merchant/test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq/receipt/5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6"
        },
        "checkout": {
        "url": "https://api.tosspayments.com/v1/payments/5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6/checkout"
        },
        "discount": null,
        "cancels": null,
        "secret": null,
        "type": "NORMAL",
        "easyPay": null,
        "country": "KR",
        "failure": null,
        "totalAmount": 15000,
        "balanceAmount": 15000,
        "suppliedAmount": 13636,
        "vat": 1364,
        "taxFreeAmount": 0,
        "taxExemptionAmount": 0
}
*/
