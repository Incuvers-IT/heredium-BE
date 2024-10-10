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

  @JsonProperty("detail_image")
  private DetailImage detailImage;

  public PostResponse(
      Long id,
      String name,
      String imageUrl,
      String originalFileName,
      Boolean isEnabled,
      String contentDetail,
      String navigationLink,
      String createdName,
      LocalDateTime createdDate,
      String thumbnailUrls) {
    this.id = id;
    this.name = name;
    this.isEnabled = isEnabled;
    this.contentDetail = contentDetail;
    this.navigationLink = navigationLink;
    this.createdName = createdName;
    this.createdDate = createdDate;

    if (thumbnailUrls != null && !thumbnailUrls.isEmpty()) {
      this.thumbnailUrls =
          new ThumbnailUrlResponse(Arrays.asList(thumbnailUrls.split(THUMBNAIL_URL_DELIMITER)));
    }

    this.detailImage = new DetailImage(imageUrl, originalFileName);
  }

  @Getter
  @Setter
  public static class DetailImage {
    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("original_file_name")
    private String originalFileName;

    public DetailImage(String imageUrl, String originalFileName) {
      this.imageUrl = imageUrl;
      this.originalFileName = originalFileName;
    }
  }
}
