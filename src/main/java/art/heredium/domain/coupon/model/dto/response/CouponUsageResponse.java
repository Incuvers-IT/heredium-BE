package art.heredium.domain.coupon.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.entity.CouponType;
import art.heredium.domain.coupon.entity.CouponUsage;

@Getter
@Setter
public class CouponUsageResponse {
  private String uuid;

  @JsonProperty("coupon_name")
  private String couponName;

  @JsonProperty("coupon_type")
  private CouponType couponType;

  @JsonProperty("discount_percent")
  private Integer discountPercent;

  @JsonProperty("expiration_date")
  private LocalDateTime expirationDate;

  @JsonProperty("is_permanent")
  private Boolean isPermanent;

  @JsonProperty("used_count")
  private Long usedCount;

  public CouponUsageResponse(CouponUsage couponUsage) {
    final Coupon coupon = couponUsage.getCoupon();
    this.uuid = couponUsage.getUuid();
    this.couponName = coupon.getName();
    this.couponType = coupon.getCouponType();
    this.discountPercent = coupon.getDiscountPercent();
    this.expirationDate = couponUsage.getExpirationDate();
    this.isPermanent = coupon.getIsPermanent();
    this.usedCount = couponUsage.getUsedCount();
  }
}
