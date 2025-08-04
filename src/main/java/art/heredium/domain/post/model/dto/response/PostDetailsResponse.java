package art.heredium.domain.post.model.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.*;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.exhibition.entity.Exhibition;
import art.heredium.domain.membership.model.dto.response.MembershipResponse;
import art.heredium.domain.post.entity.Post;
import art.heredium.domain.program.entity.Program;

@Getter
@Setter
@AllArgsConstructor
public class PostDetailsResponse {
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

  @JsonProperty("content_detail_mobile")
  private String contentDetailMobile;

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

  public PostDetailsResponse(
      @NonNull final Post post,
      @Nullable final List<Exhibition> futureExhibitions,
      @Nullable final List<Exhibition> ongoingExhibitions,
      @Nullable final List<Exhibition> completedExhibitions,
      @Nullable final List<Program> futurePrograms,
      @Nullable final List<Program> ongoingPrograms,
      @Nullable final List<Program> completedPrograms) {
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
    this.contentDetailMobile = post.getContentDetailMobile();
    this.subTitle = post.getSubTitle();
    this.memberships =
        post.getMemberships().stream()
            .filter(
                membership ->
                    membership.getIsEnabled() && !Boolean.TRUE.equals(membership.getIsDeleted()))
            .map(MembershipResponse::new)
            .collect(Collectors.toList());
    this.additionalInfo =
        AdditionalInfoResponse.builder()
            .futureExhibitions(futureExhibitions)
            .ongoingExhibitions(ongoingExhibitions)
            .completedExhibitions(completedExhibitions)
            .futurePrograms(futurePrograms)
            .ongoingPrograms(ongoingPrograms)
            .completedPrograms(completedPrograms)
            .build();
  }

  @Getter
  @Setter
  public static class AdditionalInfoResponse {
    @JsonProperty("exhibitions")
    private ExhibitionDto exhibitions;

    @JsonProperty("programs")
    private ProgramDto programs;

    @Builder
    public AdditionalInfoResponse(
        List<Exhibition> futureExhibitions,
        List<Exhibition> ongoingExhibitions,
        List<Exhibition> completedExhibitions,
        List<Program> futurePrograms,
        List<Program> ongoingPrograms,
        List<Program> completedPrograms) {
      this.exhibitions =
          new ExhibitionDto(futureExhibitions, ongoingExhibitions, completedExhibitions);
      this.programs = new ProgramDto(futurePrograms, ongoingPrograms, completedPrograms);
    }
  }

  @Getter
  @Setter
  public static class ExhibitionDto {
    @JsonProperty("future")
    private List<ExhibitionResponse> futureExhibitions;

    @JsonProperty("ongoing")
    private List<ExhibitionResponse> ongoingExhibitions;

    @JsonProperty("completed")
    private List<ExhibitionResponse> completedExhibitions;

    public ExhibitionDto(
        List<Exhibition> futureExhibitions,
        List<Exhibition> ongoingExhibitions,
        List<Exhibition> completedExhibitions) {
      this.futureExhibitions =
          futureExhibitions.stream().map(ExhibitionResponse::new).collect(Collectors.toList());
      this.ongoingExhibitions =
          ongoingExhibitions.stream().map(ExhibitionResponse::new).collect(Collectors.toList());
      this.completedExhibitions =
          completedExhibitions.stream().map(ExhibitionResponse::new).collect(Collectors.toList());
    }
  }

  @Getter
  @Setter
  public static class ProgramDto {
    @JsonProperty("future")
    private List<ProgramResponse> futurePrograms;

    @JsonProperty("ongoing")
    private List<ProgramResponse> ongoingPrograms;

    @JsonProperty("completed")
    private List<ProgramResponse> completedPrograms;

    public ProgramDto(
        List<Program> futurePrograms,
        List<Program> ongoingPrograms,
        List<Program> completedPrograms) {
      this.futurePrograms =
          futurePrograms.stream().map(ProgramResponse::new).collect(Collectors.toList());
      this.ongoingPrograms =
          ongoingPrograms.stream().map(ProgramResponse::new).collect(Collectors.toList());
      this.completedPrograms =
          completedPrograms.stream().map(ProgramResponse::new).collect(Collectors.toList());
    }
  }

  @Getter
  @Setter
  public static class ExhibitionResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("start_time")
    private LocalDateTime startTime;

    @JsonProperty("end_time")
    private LocalDateTime endTime;

    public ExhibitionResponse(@NonNull final Exhibition exhibition) {
      this.id = exhibition.getId();
      this.imageUrl =
          Optional.ofNullable(exhibition.getDetailImage())
              .map(Storage::getSavedFileName)
              .orElse(null);
      this.startTime = exhibition.getStartDate();
      this.endTime = exhibition.getEndDate();
    }
  }

  @Getter
  @Setter
  public static class ProgramResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("start_time")
    private LocalDateTime startTime;

    @JsonProperty("end_time")
    private LocalDateTime endTime;

    public ProgramResponse(Program program) {
      this.id = program.getId();
      this.imageUrl =
          Optional.ofNullable(program.getDetailImage()).map(Storage::getSavedFileName).orElse(null);
      this.startTime = program.getStartDate();
      this.endTime = program.getEndDate();
    }
  }

  @Getter
  @Setter
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
