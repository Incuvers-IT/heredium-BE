package art.heredium.domain.membership.model.dto.request;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.coupon.model.dto.request.MembershipCouponCreateRequest;

@Getter
@Setter
public class MembershipCreateRequest {

  @NotBlank private String name;

  @NotNull private Integer price;

  @NotEmpty @Valid private List<MembershipCouponCreateRequest> coupons;
}
