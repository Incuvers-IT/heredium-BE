package art.heredium.hanabank;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class HanaParamsRequest {
    @NotEmpty
    private String message;
    @NotEmpty
    private String mac;
    @NotEmpty
    private String nonce;
}
