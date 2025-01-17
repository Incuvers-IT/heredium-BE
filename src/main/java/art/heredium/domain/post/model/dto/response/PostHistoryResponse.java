package art.heredium.domain.post.model.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PostHistoryResponse {
  private Long postHistoryId;
  private AdminPostDetailsResponse content;
  private LocalDateTime modifiedDate;
  private String modifyUserEmail;
}
