package art.heredium.domain.program.model.dto.request;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.validator.constraints.Length;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.DiscountType;
import art.heredium.domain.common.type.HallType;
import art.heredium.domain.common.type.ProjectStateType;
import art.heredium.domain.program.entity.Program;
import art.heredium.domain.program.entity.ProgramRound;
import art.heredium.ncloud.bean.CloudStorage;

@Getter
@Setter
public class PostAdminProgramRequest {
  private Storage thumbnail;
  private Storage detailImage;

  @NotBlank
  @Length(max = 100)
  private String title;

  @NotNull
  @Length(max = 100)
  private String subtitle;

  @NotNull private List<HallType> halls = new ArrayList<>();
  @NotNull private Boolean isEnabled;
  @NotNull private LocalDateTime startDate;
  @NotNull private LocalDateTime endDate;
  @NotNull private LocalDateTime bookingDate;
  @NotBlank private String hour;
  @NotBlank private String contents;
  @NotNull private String buttonTitle;
  @NotNull private Boolean isUseButton;
  @NotNull private Boolean isNewTab;
  @NotNull private String buttonLink;
  @NotNull private List<@Valid Round> rounds = new ArrayList<>();
  @NotNull private List<@Valid Price> prices = new ArrayList<>();
  @NotNull private List<@Valid Writer> writers = new ArrayList<>();

  @Getter
  @Setter
  public static class Round {
    private Long id;
    @NotNull private LocalDateTime startDate;
    @NotNull private LocalDateTime endDate;
    @NotNull private Integer limitNumber;
  }

  @Getter
  @Setter
  public static class Price {
    private Long id;
    @NotNull private Boolean isEnabled;
    @NotNull private String type;
    @NotNull private Long price;

    private List<PostAdminProgramRequest.Price.@Valid PriceDiscount> discounts = new ArrayList<>();

    @Getter
    @Setter
    public static class PriceDiscount {
      private Long id;

      @NotNull
      @Length(max = 255)
      private String note;

      @NotNull private DiscountType type;
      @NotNull private Long price;
      @NotNull private Boolean enabled;
    }
  }

  @Getter
  @Setter
  public static class Writer {
    private Long id;

    @NotNull
    @Length(max = 30)
    private String name;

    private List<@Valid WriterInfo> infos = new ArrayList<>();

    @Getter
    @Setter
    public static class WriterInfo {
      private Long id;
      private Storage thumbnail;

      @NotNull
      @Length(max = 30)
      private String name;

      @NotNull
      @Length(max = 3000)
      private String intro;
    }
  }

  public void validate(CloudStorage cloudStorage) {
    validateImage(cloudStorage, this.thumbnail);
    this.getWriters()
        .forEach(
            writer ->
                writer
                    .getInfos()
                    .forEach(info -> validateImage(cloudStorage, info.getThumbnail())));

    if (this.getBookingDate().isAfter(this.getStartDate())) {
      throw new ApiException(ErrorCode.BAD_VALID, "예매오픈일이 시작일보다 이후일 수 없음");
    }
    if (this.getStartDate().isAfter(this.getEndDate())) {
      throw new ApiException(ErrorCode.BAD_VALID, "전시시작일이 종료일보다 이후일 수 없음");
    }
  }

  private void validateImage(CloudStorage cloudStorage, Storage image) {
    if (image != null && !cloudStorage.isExistObject(image.getSavedFileName())) {
      throw new ApiException(ErrorCode.S3_NOT_FOUND, image);
    }
  }

  public void validate(Program entity) {
    LocalDateTime bookingStartDate = entity.getBookingStartDate();
    LocalDateTime bookingEndDate = entity.getBookingEndDate();
    long originRoundCount =
        entity.getRounds().stream()
            .filter(round -> round.getStartDate().isBefore(bookingEndDate))
            .count();
    long dtoRoundCount =
        this.getRounds().stream()
            .filter(round -> round.getStartDate().isBefore(bookingEndDate))
            .count();
    if (originRoundCount > dtoRoundCount) {
      throw new ApiException(ErrorCode.BAD_VALID, "지난 회차와 예매가능한 회차는 삭제가 불가능");
    }

    ProjectStateType state = entity.getState();
    if (state != ProjectStateType.SCHEDULE) {
      if (!this.startDate.isEqual(entity.getStartDate())) {
        throw new ApiException(ErrorCode.BAD_VALID, "에매중,진행,종료인 전시는 시작일을 바꿀수 없음");
      }
      if (!this.bookingDate.isEqual(entity.getBookingDate())) {
        throw new ApiException(ErrorCode.BAD_VALID, "에매중,진행,종료인 예매일을 바꿀수 없음");
      }
    }

    if (state == ProjectStateType.BOOKING
        || state == ProjectStateType.PROGRESS
        || state == ProjectStateType.TERMINATION) {
      this.getRounds()
          .forEach(
              round -> {
                ProgramRound originRound =
                    entity.getRounds().stream()
                        .filter(
                            r -> round.getId() != null && r.getId().longValue() == round.getId())
                        .findAny()
                        .orElse(null);
                if (originRound != null) {
                  if (!originRound.getStartDate().isBefore(bookingStartDate)
                      && !originRound.getStartDate().isAfter(bookingEndDate)) {
                    if (!round.startDate.isEqual(originRound.getStartDate())
                        || !round.endDate.isEqual(originRound.getEndDate())) {
                      throw new ApiException(ErrorCode.BAD_VALID, "예매,진행,종료 상태일때 회차시간 변경 불가능");
                    }
                  }
                }
              });
      if (!entity.getEndDate().isEqual(this.getEndDate())) {
        if (this.getEndDate().isBefore(bookingEndDate)) {
          throw new ApiException(ErrorCode.BAD_VALID, "예매,진행,종료 상태일때 종료일을 예매중인 기간으로 변경 불가능");
        }
      }
    }
  }
}
