package art.heredium.domain.popup.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PutAdminPopupOrderRequest extends GetAdminPopupRequest {
    @NotNull
    private Long dragId;
    @NotNull
    private Long dropId;
}
