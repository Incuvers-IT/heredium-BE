package art.heredium.domain.coupon.model.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.entity.CouponUsage;

@Getter
@Setter
public class CouponResponseDto {
  private Long id;
  private String name;
  private String couponType;
  private Integer discountPercent;
  private String imageUrl;
  private List<CouponUsageDto> usedCoupons;
  private List<CouponUsageDto> unusedCoupons;

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
  }
}
