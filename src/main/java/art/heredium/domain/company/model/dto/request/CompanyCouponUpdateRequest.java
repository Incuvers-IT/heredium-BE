package art.heredium.domain.company.model.dto.request;

import javax.validation.constraints.Min;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.coupon.entity.CouponType;

@Getter
@Setter
@NoArgsConstructor
public class CompanyCouponUpdateRequest {

  private Long id;

  private String name;

  @JsonProperty("coupon_type")
  private CouponType couponType;

  @JsonProperty("discount_percent")
  private Integer discountPercent;

  @JsonProperty("image_url")
  private String imageUrl;

  @JsonProperty("number_of_uses")
  @Min(1)
  private Long numberOfUses;

  @JsonProperty("is_permanent")
  private Boolean isPermanent;

  @JsonProperty("period_in_days")
  @Min(1)
  private Integer periodInDays;

  @JsonProperty("is_deleted")
  private Boolean isDeleted;
}
