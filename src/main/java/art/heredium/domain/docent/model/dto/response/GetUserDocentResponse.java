package art.heredium.domain.docent.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.docent.entity.Docent;

@Getter
@Setter
public class GetUserDocentResponse {
  private Long id;
  private Storage thumbnail;
  private String title;
  private LocalDateTime startDate;
  private LocalDateTime endDate;

  public GetUserDocentResponse(Docent entity) {
    this.id = entity.getId();
    this.thumbnail = entity.getThumbnail();
    this.title = entity.getTitle();
    this.startDate = entity.getStartDate();
    this.endDate = entity.getEndDate();
  }
}
