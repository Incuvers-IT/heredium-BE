package art.heredium.domain.post.model.dto.response;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.model.dto.response.MembershipResponse;
import art.heredium.domain.post.entity.Post;

@Getter
@Setter
@AllArgsConstructor
public class AdminPostDetailsResponse {
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

  @JsonProperty("additional_info")
  private AdditionalInfoResponse additionalInfo;

  private List<MembershipResponse> memberships;

  public AdminPostDetailsResponse(@NonNull final Post post) {
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
    this.additionalInfo =
        AdditionalInfoResponse.builder()
            .futureExhibitionCount(post.getFutureExhibitionCount())
            .ongoingExhibitionCount(post.getOngoingExhibitionCount())
            .completedExhibitionCount(post.getCompletedExhibitionCount())
            .futureProgramCount(post.getFutureProgramCount())
            .ongoingProgramCount(post.getOngoingProgramCount())
            .completedProgramCount(post.getCompletedProgramCount())
            .build();
  }

  @Getter
  @Setter
  public static class AdditionalInfoResponse {
    @JsonProperty("future_exhibition_count")
    private Integer futureExhibitionCount;

    @JsonProperty("ongoing_exhibition_count")
    private Integer ongoingExhibitionCount;

    @JsonProperty("completed_exhibition_count")
    private Integer completedExhibitionCount;

    @JsonProperty("future_program_count")
    private Integer futureProgramCount;

    @JsonProperty("ongoing_program_count")
    private Integer ongoingProgramCount;

    @JsonProperty("completed_program_count")
    private Integer completedProgramCount;

    @Builder
    public AdditionalInfoResponse(
        Integer futureExhibitionCount,
        Integer ongoingExhibitionCount,
        Integer completedExhibitionCount,
        Integer futureProgramCount,
        Integer ongoingProgramCount,
        Integer completedProgramCount) {
      this.futureExhibitionCount = futureExhibitionCount;
      this.ongoingExhibitionCount = ongoingExhibitionCount;
      this.completedExhibitionCount = completedExhibitionCount;
      this.futureProgramCount = futureProgramCount;
      this.ongoingProgramCount = ongoingProgramCount;
      this.completedProgramCount = completedProgramCount;
    }
  }
}
