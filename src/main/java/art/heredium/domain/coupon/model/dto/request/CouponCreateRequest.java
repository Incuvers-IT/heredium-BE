package art.heredium.domain.coupon.model.dto.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.coupon.entity.CouponType;

@Getter
@Setter
public abstract class CouponCreateRequest {

  @NotBlank private String name;

  @JsonProperty("coupon_type")
  @NotNull
  private CouponType couponType;

  @JsonProperty("discount_percent")
  @NotNull
  private Integer discountPercent;

  @JsonProperty("image_url")
  @NotBlank
  private String imageUrl;

  @JsonProperty("number_of_uses")
  @Min(1)
  private Long numberOfUses;

  @JsonProperty("is_permanent")
  @NotNull
  private Boolean isPermanent;

  /** 반복형 쿠폰 여부 (true: 반복, false: 일회성) */
  @JsonProperty("is_recurring")
  @NotNull
  private Boolean isRecurring;

  /** 반복형 쿠폰 여부 (true: 반복, false: 일회성) */
  @JsonProperty("marketing_consent_benefit")
  @NotNull
  private Boolean marketingConsentBenefit;
}
