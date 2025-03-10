package art.heredium.domain.membership.model.dto.response;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.coupon.model.dto.response.CouponResponse;
import art.heredium.domain.membership.entity.Membership;

@Setter
@Getter
@NoArgsConstructor
public class MembershipResponse {

  @JsonProperty("membership_id")
  private long membershipId;

  private String name;

  private Long period;

  private List<CouponResponse> coupons;

  private Integer price;

  @JsonProperty("image_url")
  private String imageUrl;

  @JsonProperty("is_enabled")
  private Boolean isEnabled;

  @JsonProperty("is_register_membership_button_shown")
  private Boolean isRegisterMembershipButtonShown;

  public MembershipResponse(Membership membership) {
    this.coupons =
        membership.getCoupons() != null
            ? membership.getCoupons().stream().map(CouponResponse::new).collect(Collectors.toList())
            : Collections.emptyList();
    this.name = membership.getName();
    this.period = membership.getPeriod();
    this.membershipId = membership.getId();
    this.price = membership.getPrice();
    this.imageUrl = membership.getImageUrl();
    this.isEnabled = membership.getIsEnabled();
    this.isRegisterMembershipButtonShown = membership.getIsRegisterMembershipButtonShown();
  }
}
