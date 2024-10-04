package art.heredium.payment.nicepayments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import feign.FeignException;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.Base64Encoder;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.payment.dto.TicketPaymentsPayRequest;
import art.heredium.payment.inf.PaymentService;
import art.heredium.payment.nicepayments.dto.request.NicePaymentsPayRequest;
import art.heredium.payment.nicepayments.dto.request.NicePaymentsRefundRequest;
import art.heredium.payment.nicepayments.dto.response.NicePaymentsPayResponse;
import art.heredium.payment.nicepayments.dto.response.NicePaymentsRefundResponse;
import art.heredium.payment.nicepayments.feign.client.NicePaymentsClient;
import art.heredium.payment.type.PaymentType;

@Slf4j
@Service
@RequiredArgsConstructor
public class NicePayments implements PaymentService<TicketPaymentsPayRequest> {
  private static final String NICE_PAYMENT_SUCCESS_CODE = "0000";
  private final NicePaymentsClient nicePaymentsClient;
  private final Environment environment;

  @Override
  public NicePaymentsPayResponse pay(TicketPaymentsPayRequest dto, Long amount) {
    String authorization = getAuthorization();

    try {
      NicePaymentsPayResponse nicePaymentResponse =
          nicePaymentsClient.pay(
              authorization, dto.getPaymentKey(), NicePaymentsPayRequest.from(dto));
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
  public void cancel(Ticket ticket, TicketPaymentsPayRequest dto) {
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
}
