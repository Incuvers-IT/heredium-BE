package art.heredium.domain.membership.model.dto.request;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.coupon.model.dto.request.CouponCreateRequest;

@Getter
@Setter
public class MembershipCreateRequest {

  @NotBlank private String name;

  @NotNull private Integer price;

  @JsonProperty("image_url")
  private String imageUrl;

  @NotEmpty @Valid private List<CouponCreateRequest> coupons;
}
