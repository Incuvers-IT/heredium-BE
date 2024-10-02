package art.heredium.domain.coupon.model.dto.response;

import java.util.List;

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
  private List<CouponUsageDto> usedCouponUsages;
  private List<CouponUsageDto> unusedCouponUsages;

  public CouponResponseDto(
      Coupon coupon, List<CouponUsage> usedCouponUsages, List<CouponUsage> unusedCouponUsages) {
    this.id = coupon.getId();
    this.name = coupon.getName();
    this.couponType = coupon.getCouponType().name();
    this.discountPercent = coupon.getDiscountPercent();
    this.imageUrl = coupon.getImageUrl();
    this.usedCouponUsages = usedCouponUsages.stream().map(CouponUsageDto::new).toList();
    this.unusedCouponUsages = unusedCouponUsages.stream().map(CouponUsageDto::new).toList();
  }
}
