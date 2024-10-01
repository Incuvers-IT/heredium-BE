package art.heredium.domain.account.model.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutAdminAccountRequest {
  @Email private String email;
  @NotNull private Boolean isLocalResident;
}
