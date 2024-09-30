package art.heredium.domain.account.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class PutAdminPasswordRequest {
    @NotBlank
    private String password;
    @NotBlank
    private String changePassword;
}
