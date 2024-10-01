package art.heredium.domain.account.model.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.account.type.AuthType;

@Getter
@Setter
public class PostAdminRequest {
  @Email private String email;
  @NotEmpty private String name;
  @NotEmpty private String password;
  @NotEmpty private String phone;
  @NotNull private AuthType auth;
}
