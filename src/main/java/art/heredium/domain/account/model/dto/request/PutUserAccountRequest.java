package art.heredium.domain.account.model.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutUserAccountRequest {
  @Email private String email;
  private String password;
  @NotNull private Boolean isLocalResident;
  @NotNull private Boolean isMarketingReceive;
  private String encodeData;

  private String gender;         // "M" 또는 "F"
  private String birthDate;      // 1990-01-01
  private String state;          // 대전광역시
  private String district;       // 동구
  @NotNull private Boolean marketingPending;  // 0: false, 1: true(defualt)
}
