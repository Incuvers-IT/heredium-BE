package art.heredium.domain.policy.model.dto.request;

import art.heredium.domain.policy.type.PolicyType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class PostAdminPolicyRequest {
    @NotEmpty
    private String title;
    @NotNull
    private Boolean isEnabled;
    @NotNull
    private LocalDateTime postDate;
    @NotEmpty
    private String contents;
    @NotNull
    private PolicyType type;
}