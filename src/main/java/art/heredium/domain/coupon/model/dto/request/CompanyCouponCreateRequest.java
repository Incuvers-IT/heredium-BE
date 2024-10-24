package art.heredium.domain.coupon.model.dto.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
public class CompanyCouponCreateRequest extends CouponCreateRequest {

  @JsonProperty("period_in_days")
  @NotNull
  @Min(1)
  private Integer periodInDays;
}
