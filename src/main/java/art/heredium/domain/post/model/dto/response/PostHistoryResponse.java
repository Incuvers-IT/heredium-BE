package art.heredium.domain.post.model.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PostHistoryResponse {

  @JsonProperty("post_history_id")
  private Long postHistoryId;

  @JsonProperty("content")
  private String content;

  @JsonProperty("modified_date")
  private LocalDateTime modifiedDate;

  @JsonProperty("modify_user_email")
  private String modifyUserEmail;

  @JsonProperty("modify_user_name")
  private String modifyUserName;
}
