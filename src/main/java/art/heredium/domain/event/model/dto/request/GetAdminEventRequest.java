package art.heredium.domain.event.model.dto.request;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAdminEventRequest {
  private String text;
  @NotNull private Boolean isProgress;
  @NotNull private Boolean isShowDisabled;
}
