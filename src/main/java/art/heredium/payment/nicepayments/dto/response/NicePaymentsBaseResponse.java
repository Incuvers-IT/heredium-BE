package art.heredium.payment.nicepayments.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NicePaymentsBaseResponse {
  private String resultCode;
  private String resultMsg;
  private String tid;
  private String cancelledTid;
  private String orderId;
  private String ediDate;
  private String signature;
  private String status;
  private String paidAt;
  private String failedAt;
  private String cancelledAt;
  private String payMethod;
  private Integer amount;
  private Integer balanceAmt;
  private String goodsName;
  private String mallReserved;
  private Boolean useEscrow;
  private String currency;
  private String channel;
  private String approveNo;
  private String buyerName;
  private String buyerTel;
  private String buyerEmail;
  private String receiptUrl;
  private String mallUserId;
  private Boolean issuedCashReceipt;
  private Coupon coupon;
  private Card card;
  private VBank vbank;
  private Bank bank;
  private String cellphone;
  private List<Cancel> cancels;
  private String cashReceipts;
  private String messageSource;

  @Getter
  @Setter
  private static class Card {
    private String cardCode;
    private String cardName;
    private String cardNum;
    private Integer cardQuota;
    private Boolean isInterestFree;
    private String cardType;
    private Boolean canPartCancel;
    private String acquCardCode;
    private String acquCardName;
  }

  @Getter
  @Setter
  private static class Coupon {
    private Integer couponAmt;
  }

  @Getter
  @Setter
  private static class VBank {
    private String vbankCode;
    private String vbankName;
    private String vbankNumber;
    private String vbankExpDate;
    private String vbankHolder;
  }

  @Getter
  @Setter
  private static class Bank {
    private String bankCode;
    private String bankName;
  }

  @Getter
  @Setter
  private static class Cancel {
    private String tid;
    private Integer amount;
    private String cancelledAt;
    private String reason;
    private String receiptUrl;
    private Integer couponAmt;
  }
}
