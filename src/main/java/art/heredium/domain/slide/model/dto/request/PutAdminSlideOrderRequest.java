package art.heredium.domain.slide.model.dto.request;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutAdminSlideOrderRequest extends GetAdminSlideRequest {
  @NotNull private Long dragId;
  @NotNull private Long dropId;
}
