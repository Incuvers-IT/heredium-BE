package art.heredium.domain.membership.model.dto.response;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.coupon.entity.CouponType;

@Getter
@Setter
public class CouponCountByTypeResponse {

  @JsonProperty("type")
  private String type;

  @JsonProperty("quantity")
  private Long quantity;

  public CouponCountByTypeResponse(@NonNull final CouponType couponType, final long quantity) {
    this.type = couponType.getDesc();
    this.quantity = quantity;
  }
}
