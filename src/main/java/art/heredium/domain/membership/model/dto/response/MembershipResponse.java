package art.heredium.domain.membership.model.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import art.heredium.domain.coupon.entity.Coupon;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.coupon.model.dto.response.CouponResponse;
import art.heredium.domain.membership.entity.Membership;
import lombok.extern.slf4j.Slf4j;

@Setter
@Getter
@NoArgsConstructor
@Slf4j
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
      log.info("Membership coupons: {}", membership.getCoupons().stream().map(Coupon::getName).collect(Collectors.joining(",")));
      this.coupons =
        membership.getCoupons().stream().map(CouponResponse::new).collect(Collectors.toList());
    this.name = membership.getName();
    this.period = membership.getPeriod();
    this.membershipId = membership.getId();
    this.price = membership.getPrice();
    this.imageUrl = membership.getImageUrl();
    this.isEnabled = membership.getIsEnabled();
    this.isRegisterMembershipButtonShown = membership.getIsRegisterMembershipButtonShown();
  }
}
