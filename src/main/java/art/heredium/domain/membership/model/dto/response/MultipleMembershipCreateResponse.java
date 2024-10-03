package art.heredium.domain.membership.model.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MultipleMembershipCreateResponse {
  private List<Long> membershipIds;
}
