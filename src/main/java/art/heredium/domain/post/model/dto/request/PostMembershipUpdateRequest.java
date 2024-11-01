package art.heredium.domain.post.model.dto.request;

import java.util.List;

import javax.validation.Valid;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class PostMembershipUpdateRequest {
  private Long id;

  private String name;

  private Integer price;

  @JsonProperty("image_url")
  private String imageUrl;

  private Long period;

  @JsonProperty("is_enabled")
  private Boolean isEnabled;

  @JsonProperty("is_register_membership_button_shown")
  private Boolean isRegisterMembershipButtonShown;

  @Valid private List<MembershipCouponUpdateRequest> coupons;
}
