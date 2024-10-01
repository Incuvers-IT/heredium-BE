package art.heredium.domain.program.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.ProjectStateType;
import art.heredium.domain.program.entity.Program;

@Getter
@Setter
public class GetUserProgramResponse {
  private Long id;
  private Storage thumbnail;
  private ProjectStateType state;
  private String title;
  private String subtitle;
  private LocalDateTime startDate;
  private LocalDateTime endDate;

  public GetUserProgramResponse(Program entity) {
    this.id = entity.getId();
    this.thumbnail = entity.getThumbnail();
    this.state = entity.getState();
    this.title = entity.getTitle();
    this.subtitle = entity.getSubtitle();
    this.startDate = entity.getStartDate();
    this.endDate = entity.getEndDate();
  }
}
