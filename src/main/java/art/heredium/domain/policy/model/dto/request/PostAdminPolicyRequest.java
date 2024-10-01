package art.heredium.domain.policy.model.dto.request;

import java.time.LocalDateTime;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.policy.type.PolicyType;

@Getter
@Setter
public class PostAdminPolicyRequest {
  @NotEmpty private String title;
  @NotNull private Boolean isEnabled;
  @NotNull private LocalDateTime postDate;
  @NotEmpty private String contents;
  @NotNull private PolicyType type;
}
