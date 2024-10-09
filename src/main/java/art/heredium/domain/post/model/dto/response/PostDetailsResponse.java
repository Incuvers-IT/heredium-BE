package art.heredium.domain.post.model.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.membership.model.dto.response.MembershipResponse;
import art.heredium.domain.post.entity.Post;

@Getter
@Setter
@AllArgsConstructor
public class PostDetailsResponse {

  private Long id;

  private String name;

  @JsonProperty("image_url")
  private String imageUrl;

  @JsonProperty("thumbnail_url")
  private String thumbnailUrls;

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
    this.thumbnailUrls = post.getThumbnailUrls();
    this.isEnabled = post.getIsEnabled();
    this.contentDetail = post.getContentDetail();
    this.navigationLink = post.getNavigationLink();
    this.memberships =
        post.getMemberships().stream().map(MembershipResponse::new).collect(Collectors.toList());
  }
}
