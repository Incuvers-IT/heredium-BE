package art.heredium.payment.tosspayments.feign.interceptor;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@RequiredArgsConstructor
public class TossPaymentsFeignRequestInterceptor implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate template) {
    template.header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
  }
}
