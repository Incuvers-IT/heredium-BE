package art.heredium.domain.coupon.model.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.coupon.entity.CouponType;

@Getter
@Setter
public class MembershipCouponCreateRequest {

  @NotBlank(message = " can not be blank")
  private String name;

  @JsonProperty("coupon_type")
  @NotNull(message = " can not be null")
  private CouponType couponType;

  @JsonProperty("discount_percent")
  @NotNull(message = " can not be null")
  private Integer discountPercent;

  @JsonProperty("period_in_days")
  @NotNull(message = " can not be null")
  private Integer periodInDays;

  @JsonProperty("image_url")
  @NotBlank(message = " can not be blank")
  private String imageUrl;

  @JsonProperty("number_of_uses")
  private Long numberOfUses;

  @JsonProperty("is_permanent")
  @NotNull(message = " can not be null")
  private Boolean isPermanent;
}
