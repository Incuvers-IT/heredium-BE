package art.heredium.domain.coupon.model.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.entity.CouponUsage;

@Getter
@Setter
public class CouponResponseDto {
  private Long id;
  private String name;

  @JsonProperty("coupon_type")
  private String couponType;

  @JsonProperty("discount_percent")
  private Integer discountPercent;

  @JsonProperty("image_url")
  private String imageUrl;

  @JsonProperty("used_coupons")
  private List<CouponUsageDto> usedCoupons;

  @JsonProperty("unused_coupons")
  private List<CouponUsageDto> unusedCoupons;

  @JsonProperty("display_name")
  private String displayName;

  public CouponResponseDto(
      Coupon coupon, List<CouponUsage> usedCoupons, List<CouponUsage> unusedCoupons) {
    this.id = coupon.getId();
    this.name = coupon.getName();
    this.couponType = coupon.getCouponType().name();
    this.discountPercent = coupon.getDiscountPercent();
    this.imageUrl = coupon.getImageUrl();
    this.usedCoupons = usedCoupons.stream().map(CouponUsageDto::new).collect(Collectors.toList());
    this.unusedCoupons =
        unusedCoupons.stream().map(CouponUsageDto::new).collect(Collectors.toList());
    this.displayName     = coupon.getMembership() != null
            ? coupon.getMembership().getName()
            : coupon.getName();
  }
}
