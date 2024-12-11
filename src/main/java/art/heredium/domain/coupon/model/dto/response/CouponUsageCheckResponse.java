package art.heredium.domain.coupon.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@AllArgsConstructor
public class CouponUsageCheckResponse {
  @JsonProperty("number_of_used_coupons")
  private long numberOfUsedCoupons;
}
