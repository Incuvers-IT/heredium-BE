package art.heredium.domain.policy.model.dto.request;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.policy.type.PolicyType;

@Getter
@Setter
public class GetAdminPolicyPostCheckRequest {
  @NotNull private LocalDateTime postDate;
  @NotNull private PolicyType type;
}
