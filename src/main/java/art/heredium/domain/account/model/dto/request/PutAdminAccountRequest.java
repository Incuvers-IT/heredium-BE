package art.heredium.domain.account.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PutAdminAccountRequest {
    @Email
    private String email;
    @NotNull
    private Boolean isLocalResident;
}