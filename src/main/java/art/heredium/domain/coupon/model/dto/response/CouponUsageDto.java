package art.heredium.domain.coupon.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.coupon.entity.CouponUsage;

@Getter
@Setter
public class CouponUsageDto {
  private Long id;
  private String deliveredDate;
  private String usedDate;
  private String expirationDate;

  public CouponUsageDto(CouponUsage couponUsage) {
    this.id = couponUsage.getId();
    this.deliveredDate = couponUsage.getDeliveredDate().toString();
    this.usedDate = couponUsage.getUsedDate() != null ? couponUsage.getUsedDate().toString() : null;
    this.expirationDate = couponUsage.getExpirationDate().toString();
  }
}
