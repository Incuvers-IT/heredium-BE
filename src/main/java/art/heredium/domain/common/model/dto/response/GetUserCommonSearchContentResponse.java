package art.heredium.domain.common.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import com.querydsl.core.annotations.QueryProjection;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.DateState;

@Getter
@Setter
public class GetUserCommonSearchContentResponse {
  private Long id;
  private String title;
  private String subTitle;
  private Storage thumbnail;
  private LocalDateTime createdDate;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private Boolean isNotice;
  private DateState state;

  @QueryProjection
  public GetUserCommonSearchContentResponse(
      Long id,
      String title,
      Storage thumbnail,
      LocalDateTime createdDate,
      LocalDateTime startDate,
      LocalDateTime endDate) {
    this.id = id;
    this.title = title;
    this.thumbnail = thumbnail;
    this.createdDate = createdDate;
    this.state = DateState.getState(startDate, endDate);
    this.startDate = startDate;
    this.endDate = endDate;
  }

  @QueryProjection
  public GetUserCommonSearchContentResponse(
      Long id,
      String title,
      String subTitle,
      Storage thumbnail,
      LocalDateTime createdDate,
      LocalDateTime startDate,
      LocalDateTime endDate) {
    this.id = id;
    this.title = title;
    this.subTitle = subTitle;
    this.thumbnail = thumbnail;
    this.createdDate = createdDate;
    this.state = DateState.getState(startDate, endDate);
    this.startDate = startDate;
    this.endDate = endDate;
  }

  @QueryProjection
  public GetUserCommonSearchContentResponse(
      Long id, String title, Boolean isNotice, LocalDateTime createdDate) {
    this.id = id;
    this.title = title;
    this.isNotice = isNotice;
    this.createdDate = createdDate;
  }
}
