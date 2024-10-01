package art.heredium.domain.slide.model.dto.request;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAdminSlideRequest {
  private String text;
  @NotNull private Boolean isProgress;
  @NotNull private Boolean isShowDisabled;
}
