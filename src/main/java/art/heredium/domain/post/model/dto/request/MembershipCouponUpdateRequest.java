package art.heredium.domain.post.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.coupon.entity.CouponType;

@Getter
@Setter
public class MembershipCouponUpdateRequest {
  private Long id;

  private String name;

  @JsonProperty("coupon_type")
  private CouponType couponType;

  @JsonProperty("discount_percent")
  private Integer discountPercent;

  @JsonProperty("period_in_days")
  private Integer periodInDays;

  @JsonProperty("image_url")
  private String imageUrl;

  @JsonProperty("number_of_uses")
  private Long numberOfUses;

  @JsonProperty("is_permanent")
  private Boolean isPermanent;
}
