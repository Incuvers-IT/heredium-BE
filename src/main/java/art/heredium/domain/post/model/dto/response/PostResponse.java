package art.heredium.domain.post.model.dto.response;

import java.time.LocalDateTime;
import java.util.Arrays;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class PostResponse {

  private static final String THUMBNAIL_URL_DELIMITER = ";";

  private Long id;

  private String name;

  @JsonProperty("image_url")
  private String imageUrl;

  @JsonProperty("is_enabled")
  private Boolean isEnabled;

  @JsonProperty("content_detail")
  private String contentDetail;

  @JsonProperty("navigation_link")
  private String navigationLink;

  @JsonProperty("thumbnail_urls")
  private ThumbnailUrlResponse thumbnailUrls;

  @JsonProperty("created_name")
  private String createdName;

  @JsonProperty("created_date")
  private LocalDateTime createdDate;

  public PostResponse(
      Long id,
      String name,
      String imageUrl,
      Boolean isEnabled,
      String contentDetail,
      String navigationLink,
      String createdName,
      LocalDateTime createdDate,
      String thumbnailUrls) {
    this.id = id;
    this.name = name;
    this.imageUrl = imageUrl;
    this.isEnabled = isEnabled;
    this.contentDetail = contentDetail;
    this.navigationLink = navigationLink;
    this.createdName = createdName;
    this.createdDate = createdDate;

    if (thumbnailUrls != null && !thumbnailUrls.isEmpty()) {
      this.thumbnailUrls =
          new ThumbnailUrlResponse(Arrays.asList(thumbnailUrls.split(THUMBNAIL_URL_DELIMITER)));
    }
  }
}
