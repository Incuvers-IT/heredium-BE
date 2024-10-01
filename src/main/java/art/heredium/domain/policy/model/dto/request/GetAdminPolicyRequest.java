package art.heredium.domain.policy.model.dto.request;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.policy.type.PolicyType;

@Getter
@Setter
public class GetAdminPolicyRequest {
  private String text;
  @NotNull private PolicyType type;
}
