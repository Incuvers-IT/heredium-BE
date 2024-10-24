package art.heredium.domain.coupon.model.dto.request;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class NonMembershipCouponCreateRequest extends CouponCreateRequest {

  @JsonProperty("start_date")
  @NotNull
  private LocalDateTime startDate;

  @JsonProperty("end_date")
  @NotNull
  private LocalDateTime endDate;
}
