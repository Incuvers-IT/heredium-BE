package art.heredium.domain.account.model.dto.request;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutAdminPasswordRequest {
  @NotBlank private String password;
  @NotBlank private String changePassword;
}
