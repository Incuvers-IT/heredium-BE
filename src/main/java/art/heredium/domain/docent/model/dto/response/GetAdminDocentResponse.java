package art.heredium.domain.docent.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.DateState;
import art.heredium.domain.common.type.HallType;
import art.heredium.domain.docent.entity.Docent;

@Getter
@Setter
public class GetAdminDocentResponse {
  private Long id;
  private Storage thumbnail;
  private String title;
  private List<HallType> halls;
  private Boolean isEnabled;
  private DateState state;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private LocalDateTime createdDate;
  private String createdName;
  private LocalDateTime lastModifiedDate;
  private String lastModifiedName;

  public GetAdminDocentResponse(Docent entity) {
    this.id = entity.getId();
    this.thumbnail = entity.getThumbnail();
    this.title = entity.getTitle();
    this.halls = entity.getHalls();
    this.isEnabled = entity.getIsEnabled();
    this.state = entity.getState();
    this.startDate = entity.getStartDate();
    this.endDate = entity.getEndDate();
    this.createdDate = entity.getCreatedDate();
    this.createdName = entity.getCreatedName();
    this.lastModifiedDate = entity.getLastModifiedDate();
    this.lastModifiedName = entity.getLastModifiedName();
  }
}
