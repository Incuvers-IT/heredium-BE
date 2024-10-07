package art.heredium.domain.membership.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class RegisterMembershipRequest {
  @JsonProperty("membership_id")
  private long membershipId;
}
