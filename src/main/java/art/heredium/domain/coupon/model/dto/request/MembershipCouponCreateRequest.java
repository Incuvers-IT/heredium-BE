package art.heredium.domain.coupon.model.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.coupon.entity.CouponType;

@Getter
@Setter
public class MembershipCouponCreateRequest {

  @NotBlank() private String name;

  @NotNull() private CouponType couponType;

  @NotNull() private Integer discountPercent;

  @NotNull() private Integer periodInDays;

  @NotBlank() private String imageUrl;
}
