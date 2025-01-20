package art.heredium.domain.post.model.dto.request;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class PostHistorySearchRequest {
  @JsonProperty("modify_date_from")
  private Optional<LocalDateTime> modifyDateFrom;

  @JsonProperty("modify_date_to")
  private Optional<LocalDateTime> modifyDateTo;

  @JsonProperty("modify_user")
  private Optional<String> modifyUserEmailOrName;
}
