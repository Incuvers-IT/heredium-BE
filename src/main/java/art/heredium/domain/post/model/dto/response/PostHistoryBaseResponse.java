package art.heredium.domain.post.model.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostHistoryBaseResponse {
  private Long id;
  private LocalDateTime modifyDate;
  private String modifyUserEmail;
}
