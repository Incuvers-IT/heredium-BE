package art.heredium.domain.membership.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.payment.type.PaymentType;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MembershipRefundResponse {

  @JsonProperty("payment_key")
  private String paymentKey;

  @JsonProperty("payment_order_id")
  private String paymentOrderId;

  @JsonProperty("payment_type")
  private PaymentType paymentType;
}
