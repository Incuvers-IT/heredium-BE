package art.heredium.domain.post.model.dto.response;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import art.heredium.domain.membership.entity.Membership;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.membership.model.dto.response.MembershipResponse;
import art.heredium.domain.post.entity.Post;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class AdminPostDetailsResponse {
  private static final String THUMBNAIL_URL_DELIMITER = ";";

  private Long id;

  private String name;

  @JsonProperty("note_image")
  private NoteImageResponse noteImage;

  @JsonProperty("thumbnail_urls")
  private ThumbnailUrlResponse thumbnailUrls;

  @JsonProperty("is_enabled")
  private Boolean isEnabled;

  @JsonProperty("content_detail")
  private String contentDetail;

  @JsonProperty("sub_title")
  private String subTitle;

  @JsonProperty("additional_info")
  private AdditionalInfoResponse additionalInfo;

  private List<MembershipResponse> memberships;

  @JsonProperty("start_date")
  private LocalDate startDate;

  @JsonProperty("end_date")
  private LocalDate endDate;

  @JsonProperty(value = "open_date")
  private LocalDate openDate;

  public AdminPostDetailsResponse(@NonNull final Post post) {
    this.id = post.getId();
    this.name = post.getName();
    this.startDate = post.getStartDate();
    this.endDate = post.getEndDate();
    this.openDate = post.getOpenDate();
    this.noteImage =
        NoteImageResponse.builder()
            .noteImageUrl(post.getImageUrl())
            .originalFileName(post.getImageOriginalFileName())
            .build();
    if (post.getThumbnailUrls() != null) {
      this.thumbnailUrls =
          new ThumbnailUrlResponse(
              Arrays.asList(post.getThumbnailUrls().split(THUMBNAIL_URL_DELIMITER)));
    }
    this.isEnabled = post.getIsEnabled();
    this.contentDetail = post.getContentDetail();
    this.subTitle = post.getSubTitle();
    log.info("Memberships: {}", post.getMemberships().stream().map(Membership::getName).collect(Collectors.joining(",")));
    this.memberships =
        post.getMemberships().stream()
            .filter(membership -> !Boolean.TRUE.equals(membership.getIsDeleted()))
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
  @NoArgsConstructor
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

  @Getter
  @Setter
  @NoArgsConstructor
  public static class NoteImageResponse {
    @JsonProperty("note_image_url")
    private String noteImageUrl;

    @JsonProperty("original_file_name")
    private String originalFileName;

    @Builder
    public NoteImageResponse(final String noteImageUrl, final String originalFileName) {
      this.noteImageUrl = noteImageUrl;
      this.originalFileName = originalFileName;
    }
  }
}
