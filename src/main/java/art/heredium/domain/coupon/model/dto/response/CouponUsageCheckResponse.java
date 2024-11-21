package art.heredium.domain.coupon.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@AllArgsConstructor
public class CouponUsageCheckResponse {
  @JsonProperty("already_used_coupons_from_current_active_membership")
  private boolean alreadyUsedCouponsFromCurrentActiveMembership;
}
