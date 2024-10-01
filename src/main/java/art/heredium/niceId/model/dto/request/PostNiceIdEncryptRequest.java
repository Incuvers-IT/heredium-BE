package art.heredium.niceId.model.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostNiceIdEncryptRequest {
  private String returnUrl;
  private String errorUrl;
}
