package art.heredium.payment.tosspayments;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.Base64Util;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.payment.dto.PaymentsPayRequest;
import art.heredium.payment.inf.PaymentService;
import art.heredium.payment.tosspayments.dto.error.TossErrorResponse;
import art.heredium.payment.tosspayments.dto.request.TossPaymentsPayRequest;
import art.heredium.payment.tosspayments.dto.request.TossPaymentsRefundRequest;
import art.heredium.payment.tosspayments.dto.response.TossPaymentsPayResponse;
import art.heredium.payment.tosspayments.feign.client.TossPaymentsClient;
import art.heredium.payment.type.PaymentType;

@Service
@RequiredArgsConstructor
@Slf4j
public class TossPayments implements PaymentService<PaymentsPayRequest> {

  private final TossPaymentsClient client;

  private final Environment environment;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public TossPaymentsPayResponse pay(PaymentsPayRequest dto, Long amount) {
    try {
      // TODO: Will be fixed
      //            String authorization = getAuthorization(dto.getType());
      //            return client.pay(authorization, TossPaymentsPayRequest.from(dto));
      return confirmPaymentWithRequestPaymentType(dto);
    } catch (FeignException e) {
      // TODO: Will be fixed
      //      e.printStackTrace();
      //      throw new ApiException(ErrorCode.BAD_REQUEST, e.responseBody());
      try {
        return confirmPaymentWithIosKey(dto);
      } catch (FeignException ex) {
        try {
          return confirmPaymentWithAndroidKey(dto);
        } catch (FeignException exception) {
          try {
            return confirmPaymentWithWebKey(dto);
          } catch (FeignException excep) {
            excep.printStackTrace();
            throw new ApiException(ErrorCode.BAD_REQUEST, excep.responseBody());
          }
        }
      }
    }
  }

  @Override
  public void cancel(Ticket ticket, PaymentsPayRequest dto) {
    refund(ticket);
  }

  @Override
  public void refund(Ticket ticket) {
    this.handleRefund(ticket.getPgId(), ticket.getPayment());
  }

  @Override
  public void refund(String paymentKey, String paymentOrderId, PaymentType paymentType) {
    this.handleRefund(paymentKey, paymentType);
  }

  private void handleRefund(String paymentKey, PaymentType paymentType) {
    TossPaymentsRefundRequest payloadMap = new TossPaymentsRefundRequest();
    payloadMap.setCancelReason("환불");
    try {
      client.refund(getAuthorization(paymentType), paymentKey, payloadMap);
    } catch (FeignException e) {
      try {
        final String responseBody = this.toString(e.responseBody().get());
        log.error("Error handleRefund {}, body {}", e.getMessage(), responseBody);
        e.printStackTrace();
        String errorMessage =
            this.toTossErrorResponse(responseBody).map(TossErrorResponse::getMessage).orElse(null);
        throw new ApiException(ErrorCode.BAD_REQUEST, errorMessage);
      } catch (Exception exception) {
        log.error("Exception handleRefund ", exception);
        throw new ApiException(ErrorCode.BAD_REQUEST);
      }
    }
  }

  private String toString(ByteBuffer byteBuffer) {
    byte[] bytes = new byte[byteBuffer.remaining()];
    byteBuffer.get(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private Optional<TossErrorResponse> toTossErrorResponse(String responseBody) {
    try {
      final TossErrorResponse errorResponse =
          this.objectMapper.readValue(responseBody, TossErrorResponse.class);
      log.info("TossErrorResponse {} ", errorResponse);
      return Optional.of(errorResponse);
    } catch (JsonProcessingException e) {
      // Decode response body
      final String decodedStr = Base64Util.decode(responseBody);
      log.info("DecodedStr toTossErrorResponse {}", decodedStr);
      try {
        return Optional.of(this.objectMapper.readValue(decodedStr, TossErrorResponse.class));
      } catch (JsonProcessingException ex) {
        return Optional.empty();
      }
    }
  }

  private String getAuthorization(PaymentType type) {
    String secretKey = environment.getProperty(type.getPropertyKeyName());
    return Base64Util.encodeAuthorization(secretKey + ":");
  }

  private TossPaymentsPayResponse confirmPaymentWithRequestPaymentType(PaymentsPayRequest dto) {
    String authorization = getAuthorization(dto.getType());
    return client.pay(authorization, TossPaymentsPayRequest.from(dto));
  }

  private TossPaymentsPayResponse confirmPaymentWithWebKey(PaymentsPayRequest dto) {
    String authorization = getAuthorization(PaymentType.TOSSPAYMENTS);
    return client.pay(authorization, TossPaymentsPayRequest.from(dto));
  }

  private TossPaymentsPayResponse confirmPaymentWithAndroidKey(PaymentsPayRequest dto) {
    String authorization = getAuthorization(PaymentType.TOSSPAYMENTS_ANDROID);
    return client.pay(authorization, TossPaymentsPayRequest.from(dto));
  }

  private TossPaymentsPayResponse confirmPaymentWithIosKey(PaymentsPayRequest dto) {
    String authorization = getAuthorization(PaymentType.TOSSPAYMENTS_IOS);
    return client.pay(authorization, TossPaymentsPayRequest.from(dto));
  }
}
