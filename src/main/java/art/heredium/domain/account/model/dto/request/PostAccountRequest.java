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
  private String password;
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

  // --- 소셜 전용 필드 ---
  private String snsType;  // ex. "kakao" or "naver"
  private String snsId;    // front 에서 전달한 tokenkey
}
