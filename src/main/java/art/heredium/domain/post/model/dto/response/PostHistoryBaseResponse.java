package art.heredium.domain.post.model.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@AllArgsConstructor
public class PostHistoryBaseResponse {
  private Long id;

  @JsonProperty("modify_date")
  private LocalDateTime modifyDate;

  @JsonProperty("modify_user_email")
  private String modifyUserEmail;
}
