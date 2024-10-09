package art.heredium.domain.post.model.dto.response;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.model.dto.response.MembershipResponse;
import art.heredium.domain.post.entity.Post;

@Getter
@Setter
@AllArgsConstructor
public class PostDetailsResponse {
  private static final String THUMBNAIL_URL_DELIMITER = ";";

  private Long id;

  private String name;

  @JsonProperty("image_url")
  private String imageUrl;

  @JsonProperty("thumbnail_urls")
  private ThumbnailUrlResponse thumbnailUrls;

  @JsonProperty("is_enabled")
  private Boolean isEnabled;

  @JsonProperty("content_detail")
  private String contentDetail;

  @JsonProperty("navigation_link")
  private String navigationLink;

  private List<MembershipResponse> memberships;

  public PostDetailsResponse(@NonNull final Post post) {
    this.id = post.getId();
    this.name = post.getName();
    this.imageUrl = post.getImageUrl();
    if (post.getThumbnailUrls() != null) {
      this.thumbnailUrls =
          new ThumbnailUrlResponse(
              Arrays.asList(post.getThumbnailUrls().split(THUMBNAIL_URL_DELIMITER)));
    }
    this.isEnabled = post.getIsEnabled();
    this.contentDetail = post.getContentDetail();
    this.navigationLink = post.getNavigationLink();
    this.memberships =
        post.getMemberships().stream()
            .filter(Membership::getIsEnabled)
            .map(MembershipResponse::new)
            .collect(Collectors.toList());
  }
}
