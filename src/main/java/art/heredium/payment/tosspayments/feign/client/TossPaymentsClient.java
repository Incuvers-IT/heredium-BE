package art.heredium.payment.tosspayments.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import art.heredium.payment.tosspayments.dto.request.TossPaymentsPayRequest;
import art.heredium.payment.tosspayments.dto.request.TossPaymentsRefundRequest;
import art.heredium.payment.tosspayments.dto.response.TossPaymentsPayResponse;
import art.heredium.payment.tosspayments.feign.config.TossPaymentsFeignConfig;

@FeignClient(
    name = "tosspayments",
    url = "https://api.tosspayments.com",
    configuration = TossPaymentsFeignConfig.class)
public interface TossPaymentsClient {

  @PostMapping("/v1/payments/confirm")
  TossPaymentsPayResponse pay(
      @RequestHeader("Authorization") String authorization,
      @RequestBody TossPaymentsPayRequest request);

  @PostMapping("/v1/payments/{paymentKey}/cancel")
  void refund(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("paymentKey") String paymentKey,
      @RequestBody TossPaymentsRefundRequest request);
}
