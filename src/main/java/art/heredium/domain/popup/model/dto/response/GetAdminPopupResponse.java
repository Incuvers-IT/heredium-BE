package art.heredium.domain.popup.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.DateState;
import art.heredium.domain.popup.entity.Popup;

@Getter
@Setter
public class GetAdminPopupResponse {
  private Long id;
  private Storage pcImage;
  private String pcImageAlt;
  private String title;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private LocalDateTime lastModifiedDate;
  private Boolean isEnabled;
  private DateState state;

  public GetAdminPopupResponse(Popup entity) {
    this.id = entity.getId();
    this.pcImage = entity.getPcImage();
    this.pcImageAlt = entity.getPcImageAlt();
    this.title = entity.getTitle();
    this.startDate = entity.getStartDate();
    this.endDate = entity.getEndDate();
    this.lastModifiedDate = entity.getLastModifiedDate();
    this.isEnabled = entity.getIsEnabled();
    this.state = entity.getState();
  }
}
