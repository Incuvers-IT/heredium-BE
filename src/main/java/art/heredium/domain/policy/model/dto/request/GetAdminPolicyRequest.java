package art.heredium.domain.policy.model.dto.request;

import art.heredium.domain.policy.type.PolicyType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class GetAdminPolicyRequest {
    private String text;
    @NotNull
    private PolicyType type;
}
