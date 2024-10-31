package art.heredium.domain.coupon.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.coupon.entity.CouponUsage;

@Getter
@Setter
public class CouponUsageDto {
  private Long id;

  @JsonProperty("delivered_date")
  private LocalDateTime deliveredDate;

  @JsonProperty("used_date")
  private LocalDateTime usedDate;

  @JsonProperty("expiration_date")
  private LocalDateTime expirationDate;

  private String uuid;

  @JsonProperty("is_expired")
  private boolean isExpired;

  @JsonProperty("is_permanent")
  private boolean isPermanent;

  public CouponUsageDto(CouponUsage couponUsage) {
    this.id = couponUsage.getId();
    this.deliveredDate = couponUsage.getDeliveredDate();
    this.usedDate = couponUsage.getUsedDate();
    this.expirationDate = couponUsage.getExpirationDate();
    this.uuid = couponUsage.getUuid();
    this.isExpired = LocalDateTime.now().isAfter(couponUsage.getExpirationDate());
    this.isPermanent = couponUsage.isPermanent();
  }
}
