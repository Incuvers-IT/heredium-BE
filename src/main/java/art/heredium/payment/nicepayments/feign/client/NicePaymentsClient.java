package art.heredium.payment.nicepayments.feign.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import art.heredium.payment.nicepayments.dto.request.NicePaymentsPayRequest;
import art.heredium.payment.nicepayments.dto.request.NicePaymentsRefundRequest;
import art.heredium.payment.nicepayments.dto.response.NicePaymentsPayResponse;
import art.heredium.payment.nicepayments.dto.response.NicePaymentsRefundResponse;
import art.heredium.payment.nicepayments.feign.config.NicePaymentsFeignConfig;

@FeignClient(
    name = "nicepay",
    url = "https://sandbox-api.nicepay.co.kr/v1",
    configuration = NicePaymentsFeignConfig.class)
public interface NicePaymentsClient {

  @PostMapping("/v1/payments/{tid}")
  NicePaymentsPayResponse pay(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("tid") String tid,
      @RequestBody NicePaymentsPayRequest request);

  @PostMapping("/v1/payments/{tid}/cancel")
  NicePaymentsRefundResponse refund(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("tid") String tid,
      @RequestBody NicePaymentsRefundRequest request);
}
