package art.heredium.domain.account.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostLoginResponse {
  private String token;
  private Boolean isSleeper;
  private String name;
}
