package art.heredium.domain.membership.model.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@AllArgsConstructor
public class MultipleMembershipCreateResponse {
  @JsonProperty("membership_ids")
  private List<Long> membershipIds;
}
