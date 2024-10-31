package art.heredium.payment.tosspayments;

import lombok.RequiredArgsConstructor;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import feign.FeignException;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.Base64Encoder;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.payment.dto.PaymentsPayRequest;
import art.heredium.payment.inf.PaymentService;
import art.heredium.payment.tosspayments.dto.request.TossPaymentsPayRequest;
import art.heredium.payment.tosspayments.dto.request.TossPaymentsRefundRequest;
import art.heredium.payment.tosspayments.dto.response.TossPaymentsPayResponse;
import art.heredium.payment.tosspayments.feign.client.TossPaymentsClient;
import art.heredium.payment.type.PaymentType;

@Service
@RequiredArgsConstructor
public class TossPayments implements PaymentService<PaymentsPayRequest> {

  private final TossPaymentsClient client;

  private final Environment environment;

  @Override
  public TossPaymentsPayResponse pay(PaymentsPayRequest dto, Long amount) {
    try {
      String authorization = getAuthorization(dto.getType());
      return client.pay(authorization, TossPaymentsPayRequest.from(dto));
    } catch (FeignException e) {
      e.printStackTrace();
      throw new ApiException(ErrorCode.BAD_REQUEST, e.responseBody());
    }
  }

  @Override
  public void cancel(Ticket ticket, PaymentsPayRequest dto) {
    refund(ticket);
  }

  @Override
  public void refund(Ticket ticket) {
    TossPaymentsRefundRequest payloadMap = new TossPaymentsRefundRequest();
    payloadMap.setCancelReason("환불");

    try {
      client.refund(getAuthorization(ticket.getPayment()), ticket.getPgId(), payloadMap);
    } catch (FeignException e) {
      e.printStackTrace();
      throw new ApiException(ErrorCode.BAD_REQUEST, e.responseBody());
    }
  }

  private String getAuthorization(PaymentType type) {
    String secretKey = environment.getProperty(type.getPropertyKeyName());
    return Base64Encoder.encodeAuthorization(secretKey + ":");
  }
}
