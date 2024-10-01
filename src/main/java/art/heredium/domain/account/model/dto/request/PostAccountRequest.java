package art.heredium.domain.account.model.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostAccountRequest {
  @Email private String email;
  @NotBlank private String password;
  @NotBlank private String encodeData;
  @NotNull private Boolean isLocalResident;
  @NotNull private Boolean isMarketingReceive;
}
