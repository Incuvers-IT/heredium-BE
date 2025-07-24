package art.heredium.domain.membership.model.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundRequest {

  @JsonProperty("reason")
  private String reason;
}
