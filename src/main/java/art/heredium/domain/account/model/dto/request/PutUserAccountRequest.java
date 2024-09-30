package art.heredium.domain.account.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PutUserAccountRequest {
    @Email
    private String email;
    private String password;
    @NotNull
    private Boolean isLocalResident;
    @NotNull
    private Boolean isMarketingReceive;
    private String encodeData;
}
