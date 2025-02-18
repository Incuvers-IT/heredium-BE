package art.heredium.domain.company.model.dto.response;

import java.time.LocalDateTime;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.entity.CouponType;

@Data
public class CompanyCouponResponse {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("name")
  private String name;

  @JsonProperty("type")
  private CouponType type;

  @JsonProperty("discount_percent")
  private Integer discountPercent;

  @JsonProperty("period_in_days")
  private Integer periodInDays;

  @JsonProperty("started_date")
  private LocalDateTime startedDate;

  @JsonProperty("ended_date")
  private LocalDateTime endedDate;

  @JsonProperty("image_url")
  private String imageUrl;

  @JsonProperty("number_of_uses")
  private Long numberOfUses;

  @JsonProperty("is_permanent")
  private Boolean isPermanent;

  public CompanyCouponResponse(Coupon coupon) {
    this.id = coupon.getId();
    this.name = coupon.getName();
    this.type = coupon.getCouponType();
    this.discountPercent = coupon.getDiscountPercent();
    this.periodInDays = coupon.getPeriodInDays();
    this.startedDate = coupon.getStartedDate();
    this.endedDate = coupon.getEndedDate();
    this.imageUrl = coupon.getImageUrl();
    this.numberOfUses = coupon.getNumberOfUses();
    this.isPermanent = coupon.getIsPermanent();
  }
}
