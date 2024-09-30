package art.heredium.domain.account.model.dto.request;

import art.heredium.domain.account.type.AuthType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PostAdminRequest {
    @Email
    private String email;
    @NotEmpty
    private String name;
    @NotEmpty
    private String password;
    @NotEmpty
    private String phone;
    @NotNull
    private AuthType auth;
}
