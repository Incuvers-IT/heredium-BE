package art.heredium.domain.popup.model.dto.request;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAdminPopupRequest {
  private String text;
  @NotNull private Boolean isProgress;
  @NotNull private Boolean isShowDisabled;
}
