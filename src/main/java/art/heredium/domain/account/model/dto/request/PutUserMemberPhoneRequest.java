package art.heredium.domain.account.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class PutUserMemberPhoneRequest {
    @NotBlank
    private String encodeData;
}
