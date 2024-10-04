package art.heredium.domain.post.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GetEnabledPostsResponse {
  private Long id;
  private String name;
  private String imageUrl;
  private Boolean isEnabled;
  private String contentDetail;
  private String navigationLink;
}
