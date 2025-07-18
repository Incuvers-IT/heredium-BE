package art.heredium.domain.account.model.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostAccountSnsRequest {
  @NotBlank private String token;
  @NotBlank private String encodeData;
  @NotNull private Boolean isLocalResident;
  @NotNull private Boolean isMarketingReceive;
  @NotNull private Boolean marketingPending;
  @NotNull private Boolean additionalInfoAgreed;
  private String gender;
  private String birthDate;
  private String state;
  private String district;
  private String job;
}
