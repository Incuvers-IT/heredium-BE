package art.heredium.domain.popup.model.dto.request;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutAdminPopupOrderRequest extends GetAdminPopupRequest {
  @NotNull private Long dragId;
  @NotNull private Long dropId;
}
