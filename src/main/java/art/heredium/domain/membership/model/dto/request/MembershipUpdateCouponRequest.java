package art.heredium.domain.membership.model.dto.request;

import art.heredium.domain.post.model.dto.request.MembershipCouponUpdateRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import java.util.List;

@Setter
@Getter
public class MembershipUpdateCouponRequest {

  private String name;

  @JsonProperty("image_url")
  private String imageUrl;

  @JsonProperty("is_enabled")
  private Boolean isEnabled;

  @JsonProperty("usage_threshold")
  private Integer usageThreshold;

  private List<MembershipCouponUpdateRequest> coupons;
}
