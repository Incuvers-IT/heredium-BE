package art.heredium.domain.post.model.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class ThumbnailUrlResponse {
  @JsonProperty("small")
  private String smallThumbnailUrl;

  @JsonProperty("medium")
  private String mediumThumbnailUrl;

  @JsonProperty("large")
  private String largeThumbnailUrl;

  public ThumbnailUrlResponse(List<String> thumbnailUrls) {
    this.smallThumbnailUrl = !thumbnailUrls.isEmpty() ? thumbnailUrls.get(0) : null;
    this.mediumThumbnailUrl = thumbnailUrls.size() > 1 ? thumbnailUrls.get(1) : null;
    this.largeThumbnailUrl = thumbnailUrls.size() > 2 ? thumbnailUrls.get(2) : null;
  }
}
