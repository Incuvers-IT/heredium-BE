package art.heredium.domain.account.model.dto.request;

import art.heredium.domain.account.type.AuthType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PutAdminRequest {
    @Email
    private String email;
    @NotEmpty
    private String name;
    @NotEmpty
    private String phone;
    @NotNull
    private AuthType auth;
    @NotNull
    private Boolean isEnabled;
}
