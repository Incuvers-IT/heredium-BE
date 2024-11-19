package art.heredium.domain.coupon.model.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CouponUsageCheckResponse {
  @JsonProperty("already_used_coupons_from_current_active_membership")
  private boolean alreadyUsedCouponsFromCurrentActiveMembership;
}
