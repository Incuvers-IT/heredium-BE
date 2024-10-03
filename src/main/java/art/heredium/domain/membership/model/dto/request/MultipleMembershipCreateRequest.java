package art.heredium.domain.membership.model.dto.request;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MultipleMembershipCreateRequest {
  @NotEmpty() @Valid private List<MembershipCreateRequest> memberships;
}
