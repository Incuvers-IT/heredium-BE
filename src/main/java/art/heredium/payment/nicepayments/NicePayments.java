package art.heredium.payment.nicepayments;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import feign.FeignException;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.Base64Encoder;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.payment.dto.PaymentsPayRequest;
import art.heredium.payment.inf.PaymentService;
import art.heredium.payment.nicepayments.dto.request.NicePaymentsRefundRequest;
import art.heredium.payment.nicepayments.dto.response.NicePaymentsBaseResponse;
import art.heredium.payment.nicepayments.dto.response.NicePaymentsPayResponse;
import art.heredium.payment.nicepayments.dto.response.NicePaymentsRefundResponse;
import art.heredium.payment.nicepayments.feign.client.NicePaymentsClient;
import art.heredium.payment.type.PaymentType;

@Slf4j
@Service
@RequiredArgsConstructor
public class NicePayments implements PaymentService<PaymentsPayRequest> {
  private static final String NICE_PAYMENT_SUCCESS_CODE = "0000";
  private final NicePaymentsClient nicePaymentsClient;
  private final Environment environment;

  @Override
  public NicePaymentsPayResponse pay(PaymentsPayRequest dto, Long amount) {
    String authorization = getAuthorization();

    try {
      //      NicePaymentsPayResponse nicePaymentResponse =
      //          nicePaymentsClient.pay(
      //              authorization, dto.getPaymentKey(), NicePaymentsPayRequest.from(dto));

      // TODO: Will be remove in the future
      NicePaymentsPayResponse nicePaymentResponse = getMockedNicePaymentsPayResponse(dto);

      if (!NICE_PAYMENT_SUCCESS_CODE.equals(nicePaymentResponse.getResultCode())) {
        log.error(
            "An error occurred while paying with NICE Payment: ErrorCode: {}, ErrorMessage: {}",
            nicePaymentResponse.getResultCode(),
            nicePaymentResponse.getResultMsg());
        throw new ApiException(
            ErrorCode.BAD_REQUEST,
            "ErrorCode: "
                + nicePaymentResponse.getResultCode()
                + ", ErrorMessage: "
                + nicePaymentResponse.getResultMsg());
      }
      return nicePaymentResponse;
    } catch (FeignException e) {
      log.error("An error occurred while paying with NICE Payment", e);
      throw new ApiException(ErrorCode.BAD_REQUEST, e.responseBody());
    }
  }

  @Override
  public void cancel(Ticket ticket, PaymentsPayRequest dto) {
    refund(ticket);
  }

  @Override
  public void refund(Ticket ticket) {
    String authorization = getAuthorization();

    try {
      NicePaymentsRefundResponse refundResponse =
          nicePaymentsClient.refund(
              authorization, ticket.getPgId(), NicePaymentsRefundRequest.from(ticket.getUuid()));
      if (!NICE_PAYMENT_SUCCESS_CODE.equals(refundResponse.getResultCode())) {
        log.error(
            "An error occurred while refunding with NICE Payment: ErrorCode: {}, ErrorMessage: {}",
            refundResponse.getResultCode(),
            refundResponse.getResultMsg());
        throw new ApiException(
            ErrorCode.BAD_REQUEST,
            "ErrorCode: "
                + refundResponse.getResultCode()
                + ", ErrorMessage: "
                + refundResponse.getResultMsg());
      }
    } catch (FeignException e) {
      log.error("An error occurred while refunding with NICE Payment", e);
      throw new ApiException(ErrorCode.BAD_REQUEST, e.responseBody());
    }
  }

  private String getAuthorization() {
    String secretKey = environment.getProperty(PaymentType.NICEPAYMENTS.getPropertyKeyName());
    return Base64Encoder.encodeAuthorization(secretKey);
  }

  // TODO: Will be removed in the future
  private NicePaymentsPayResponse getMockedNicePaymentsPayResponse(PaymentsPayRequest dto) {
    NicePaymentsPayResponse nicePaymentResponse = new NicePaymentsPayResponse();
    nicePaymentResponse.setResultCode(NICE_PAYMENT_SUCCESS_CODE);
    nicePaymentResponse.setResultMsg("정상 처리되었습니다.");
    nicePaymentResponse.setTid(dto.getPaymentKey());
    nicePaymentResponse.setCancelledTid(null);
    nicePaymentResponse.setOrderId(dto.getOrderId());
    nicePaymentResponse.setEdiDate("2024-10-02T17:47:00.554+0900");
    nicePaymentResponse.setSignature(UUID.randomUUID().toString());
    nicePaymentResponse.setStatus("paid");
    nicePaymentResponse.setPaidAt("2021-11-05T17:14:35.000+0900");
    nicePaymentResponse.setFailedAt("0");
    nicePaymentResponse.setCancelledAt("0");
    nicePaymentResponse.setPayMethod("card");
    nicePaymentResponse.setAmount(dto.getAmount().intValue());
    nicePaymentResponse.setBalanceAmt(1004);
    nicePaymentResponse.setGoodsName("나이스페이-상품");
    nicePaymentResponse.setMallReserved(null);
    nicePaymentResponse.setUseEscrow(false);
    nicePaymentResponse.setCurrency("KRW");
    nicePaymentResponse.setChannel("pc");
    nicePaymentResponse.setApproveNo("000000");
    nicePaymentResponse.setBuyerName(null);
    nicePaymentResponse.setBuyerTel(null);
    nicePaymentResponse.setBuyerEmail(null);
    nicePaymentResponse.setReceiptUrl(
        "https://npg.nicepay.co.kr/issue/IssueLoader.do?type=0&innerWin=Y&TID=UT0000113m01012111051714341073");
    nicePaymentResponse.setMallUserId(null);
    nicePaymentResponse.setIssuedCashReceipt(false);
    nicePaymentResponse.setCashReceipts(null);
    nicePaymentResponse.setMessageSource("nicepay");

    NicePaymentsBaseResponse.Coupon coupon = new NicePaymentsBaseResponse.Coupon();
    coupon.setCouponAmt(0);
    nicePaymentResponse.setCoupon(coupon);

    NicePaymentsBaseResponse.Card card = new NicePaymentsBaseResponse.Card();
    card.setCardCode("04");
    card.setCardName("삼성");
    card.setCardNum("123412******1234");
    card.setCardQuota(0);
    card.setIsInterestFree(false);
    card.setCardType("credit");
    card.setCanPartCancel(true);
    card.setAcquCardCode("04");
    card.setAcquCardName("삼성");
    nicePaymentResponse.setCard(card);

    nicePaymentResponse.setVbank(null);
    nicePaymentResponse.setBank(null);

    nicePaymentResponse.setCellphone(null);
    nicePaymentResponse.setCancels(null);

    return nicePaymentResponse;
  }
}
