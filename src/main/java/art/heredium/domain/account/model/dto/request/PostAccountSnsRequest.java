package art.heredium.domain.account.model.dto.request;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostAccountSnsRequest {
  @NotBlank private String token;
  @NotBlank private String encodeData;
  private Boolean isMarketingReceive;
}
