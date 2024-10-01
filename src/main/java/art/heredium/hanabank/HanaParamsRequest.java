package art.heredium.hanabank;

import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HanaParamsRequest {
  @NotEmpty private String message;
  @NotEmpty private String mac;
  @NotEmpty private String nonce;
}
