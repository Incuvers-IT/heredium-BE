package art.heredium.domain.slide.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PutAdminSlideOrderRequest extends GetAdminSlideRequest {
    @NotNull
    private Long dragId;
    @NotNull
    private Long dropId;
}
