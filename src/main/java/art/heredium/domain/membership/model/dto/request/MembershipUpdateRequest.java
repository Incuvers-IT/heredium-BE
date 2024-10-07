package art.heredium.domain.membership.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Setter
@Getter
public class MembershipUpdateRequest {
  @JsonProperty(value = "is_enabled", required = true)
  private Boolean isEnabled;
}
