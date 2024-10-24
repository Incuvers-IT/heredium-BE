package art.heredium.payment.nicepayments.dto.response;

import art.heredium.payment.inf.PaymentTicketResponse;

public class NicePaymentsPayResponse extends NicePaymentsBaseResponse
    implements PaymentTicketResponse {
  @Override
  public Long getPaymentAmount() {
    return super.getAmount().longValue();
  }

  @Override
  public String getPaymentKey() {
    return super.getTid();
  }

  @Override
  public String getPayMethod() {
    return super.getPayMethod();
  }
}

/*
{
    "resultCode": "0000",
    "resultMsg": "정상 처리되었습니다.",
    "tid": "UT0000113m01012111051714341073",
    "cancelledTid": null,
    "orderId": "c74a5960-830b-4cd8-82a9-fa1ce739a18f",
    "ediDate": "2024-10-02T17:47:00.554+0900",
    "signature": "b67d4fa22292329043c1d428d49d022907cb6b2c86cb757007e199837fef2e34",
    "status": "paid",
    "paidAt": "2021-11-05T17:14:35.000+0900",
    "failedAt": "0",
    "cancelledAt": "0",
    "payMethod": "card",
    "amount": 1004,
    "balanceAmt": 1004,
    "goodsName": "나이스페이-상품",
    "mallReserved": null,
    "useEscrow": false,
    "currency": "KRW",
    "channel": "pc",
    "approveNo": "000000",
    "buyerName": null,
    "buyerTel": null,
    "buyerEmail": null,
    "receiptUrl": "https://npg.nicepay.co.kr/issue/IssueLoader.do?type=0&innerWin=Y&TID=UT0000113m01012111051714341073",
    "mallUserId": null,
    "issuedCashReceipt": false,
    "coupon": {
        "couponAmt": 0
    },
    "card": {
        "cardCode": "04",
        "cardName": "삼성",
        "cardNum": "123412******1234",
        "cardQuota": 0,
        "isInterestFree": false,
        "cardType": "credit",
        "canPartCancel": true,
        "acquCardCode": "04",
        "acquCardName": "삼성"
    },
    "vbank": null,
    "bank": null,
    "cellphone": null,
    "cancels": null,
    "cashReceipts": null,
    "messageSource": "nicepay"
}
*/
