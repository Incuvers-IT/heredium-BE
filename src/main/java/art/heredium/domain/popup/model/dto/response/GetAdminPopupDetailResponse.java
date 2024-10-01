package art.heredium.domain.popup.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.popup.entity.Popup;

@Getter
@Setter
public class GetAdminPopupDetailResponse {
  private Long id;
  private String title;
  private Boolean isEnabled;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private Storage pcImage;
  private String pcImageAlt;
  private Storage mobileImage;
  private String mobileImageAlt;
  private Boolean isHideToday;
  private Boolean isNewTab;
  private String link;
  private String lastModifiedName;
  private LocalDateTime lastModifiedDate;
  private String createdName;
  private LocalDateTime createdDate;

  public GetAdminPopupDetailResponse(Popup entity) {
    this.id = entity.getId();
    this.title = entity.getTitle();
    this.isEnabled = entity.getIsEnabled();
    this.startDate = entity.getStartDate();
    this.endDate = entity.getEndDate();
    this.pcImage = entity.getPcImage();
    this.pcImageAlt = entity.getPcImageAlt();
    this.mobileImage = entity.getMobileImage();
    this.mobileImageAlt = entity.getMobileImageAlt();
    this.isHideToday = entity.getIsHideToday();
    this.isNewTab = entity.getIsNewTab();
    this.link = entity.getLink();
    this.lastModifiedName = entity.getLastModifiedName();
    this.lastModifiedDate = entity.getLastModifiedDate();
    this.createdName = entity.getCreatedName();
    this.createdDate = entity.getCreatedDate();
  }
}
