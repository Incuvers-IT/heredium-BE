package art.heredium.domain.post.model.dto.request;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class PostUpdateRequest {
  private String name;

  @JsonProperty("is_enabled")
  private Boolean isEnabled;

  @JsonProperty("content_detail")
  private String contentDetail;

  @Size(max = 5000)
  @JsonProperty("sub_title")
  private String subTitle;

  @JsonProperty("thumbnail_urls")
  private ThumbnailUrl thumbnailUrls;

  @JsonProperty("note_image")
  private NoteImage noteImage;

  @Valid private List<PostMembershipUpdateRequest> memberships;

  @JsonProperty("additional_info")
  private AdditionalInfo additionalInfo;

  @JsonProperty("start_date")
  private LocalDate startDate;

  @JsonProperty("end_date")
  private LocalDate endDate;

  @JsonProperty(value = "open_date")
  private LocalDate openDate;

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

  @Getter
  @Setter
  public static class NoteImage {
    @JsonProperty("note_image_url")
    private String noteImageUrl;

    @JsonProperty("original_file_name")
    private String originalFileName;
  }

  @Getter
  @Setter
  public static class AdditionalInfo {
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
  }
}
