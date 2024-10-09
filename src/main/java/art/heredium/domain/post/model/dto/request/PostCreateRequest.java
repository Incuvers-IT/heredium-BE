package art.heredium.domain.post.model.dto.request;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.membership.model.dto.request.MultipleMembershipCreateRequest;

@Getter
@Setter
public class PostCreateRequest {
  @NotBlank private String name;

  @JsonProperty("image_url")
  @NotBlank
  private String imageUrl;

  @JsonProperty("is_enabled")
  @NotNull
  private Boolean isEnabled;

  @JsonProperty("navigation_link")
  @NotBlank
  private String navigationLink;

  @Size(max = 5000)
  @JsonProperty("content_detail")
  private String contentDetail;

  @JsonProperty("thumbnail_url")
  private ThumbnailUrl thumbnailUrl;

  @Valid private MultipleMembershipCreateRequest memberships;

  @Getter
  @Setter
  public static class ThumbnailUrl {
    @JsonProperty("small")
    private String smallThumbnailUrl;

    @JsonProperty("medium")
    private String mediumThumbnailUrl;

    @JsonProperty("large")
    private String largeThumbnailUrl;
  }
}
