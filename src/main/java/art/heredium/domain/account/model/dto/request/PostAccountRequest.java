package art.heredium.domain.account.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PostAccountRequest {
  @Email private String email;
  @NotBlank private String password;
  @NotBlank private String encodeData;
  @NotNull private Boolean isLocalResident;
  @NotNull private Boolean isMarketingReceive;
  @NotNull private Boolean marketingPending;
  private String gender;
  private String birthDate;
  private String state;
  private String district;
}
