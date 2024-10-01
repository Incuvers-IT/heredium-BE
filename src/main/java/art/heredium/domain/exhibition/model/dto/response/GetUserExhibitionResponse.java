package art.heredium.domain.exhibition.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.ProjectStateType;
import art.heredium.domain.exhibition.entity.Exhibition;

@Getter
@Setter
public class GetUserExhibitionResponse {
  private Long id;
  private Storage thumbnail;
  private ProjectStateType state;
  private String title;
  private String subtitle;
  private LocalDateTime startDate;
  private LocalDateTime endDate;

  public GetUserExhibitionResponse(Exhibition entity) {
    this.id = entity.getId();
    this.thumbnail = entity.getThumbnail();
    this.state = entity.getState();
    this.title = entity.getTitle();
    this.subtitle = entity.getSubtitle();
    this.startDate = entity.getStartDate();
    this.endDate = entity.getEndDate();
  }
}
