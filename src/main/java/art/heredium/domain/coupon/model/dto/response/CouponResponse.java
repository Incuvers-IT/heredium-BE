package art.heredium.domain.coupon.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.entity.CouponType;

@Getter
@Setter
public class CouponResponse {

  @JsonProperty("coupon_id")
  private long couponId;

  private String name;

  @JsonProperty("coupon_type")
  private CouponType couponType;

  @JsonProperty("discount_percent")
  private Integer discountPercent;

  @JsonProperty("period_in_days")
  private Integer periodInDays;

  @JsonProperty("image_url")
  private String imageUrl;

  @JsonProperty("is_permanent")
  private Boolean isPermanent;

  @JsonProperty("number_of_uses")
  private Long numberOfUses;

  @JsonProperty("start_date")
  private LocalDateTime startDate;

  @JsonProperty("end_date")
  private LocalDateTime endDate;

  public CouponResponse(Coupon coupon) {
    this.couponId = coupon.getId();
    this.name = coupon.getName();
    this.couponType = coupon.getCouponType();
    this.discountPercent = coupon.getDiscountPercent();
    this.periodInDays = coupon.getPeriodInDays();
    this.imageUrl = coupon.getImageUrl();
    this.isPermanent = coupon.getIsPermanent();
    this.numberOfUses = coupon.getNumberOfUses();
    this.startDate = coupon.getStartedDate();
    this.endDate = coupon.getEndedDate();
  }
}
