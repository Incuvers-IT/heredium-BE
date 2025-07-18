package art.heredium.domain.coupon.model.dto.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
public class MembershipCouponCreateRequest extends CouponCreateRequest {

  @JsonProperty("period_in_days")
  @NotNull
  @Min(0)
  private Integer periodInDays;
}
