package art.heredium.domain.membership.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class RegisterMembershipResponse {
  @JsonProperty("membership_registration_id")
  private long membershipRegistrationId;

  public RegisterMembershipResponse(long membershipRegistrationId) {
    this.membershipRegistrationId = membershipRegistrationId;
  }
}
