package art.heredium.domain.popup.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.popup.entity.Popup;

@Getter
@Setter
public class GetUserPopupResponse {
  private Storage pcImage;
  private String pcImageAlt;
  private Storage mobileImage;
  private String mobileImageAlt;
  private Boolean isHideToday;
  private Boolean isNewTab;
  private String link;

  public GetUserPopupResponse(Popup entity) {
    this.pcImage = entity.getPcImage();
    this.pcImageAlt = entity.getPcImageAlt();
    this.mobileImage = entity.getMobileImage();
    this.mobileImageAlt = entity.getMobileImageAlt();
    this.isHideToday = entity.getIsHideToday();
    this.isNewTab = entity.getIsNewTab();
    this.link = entity.getLink();
  }
}
