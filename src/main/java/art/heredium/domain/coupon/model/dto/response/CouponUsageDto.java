package art.heredium.domain.coupon.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.coupon.entity.CouponUsage;

@Getter
@Setter
public class CouponUsageDto {
  private Long id;
  private LocalDateTime deliveredDate;
  private LocalDateTime usedDate;
  private LocalDateTime expirationDate;
  private String uuid;

  public CouponUsageDto(CouponUsage couponUsage) {
    this.id = couponUsage.getId();
    this.deliveredDate = couponUsage.getDeliveredDate();
    this.usedDate = couponUsage.getUsedDate();
    this.expirationDate = couponUsage.getExpirationDate();
    this.uuid = couponUsage.getUuid();
  }
}
