package art.heredium.domain.post.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class PostUpdateRequest {

  @JsonProperty(value = "is_enabled", required = true)
  private Boolean isEnabled;
}
