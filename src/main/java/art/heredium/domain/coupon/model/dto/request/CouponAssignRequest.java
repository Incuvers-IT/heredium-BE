package art.heredium.domain.coupon.model.dto.request;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@Builder
public class CouponAssignRequest {

  @JsonProperty("coupon_id")
  @NotNull
  private long couponId;

  @JsonProperty("account_ids")
  @NotNull
  private List<Long> accountIds;
}
