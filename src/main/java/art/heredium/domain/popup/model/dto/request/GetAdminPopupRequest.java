package art.heredium.domain.popup.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class GetAdminPopupRequest {
    private String text;
    @NotNull
    private Boolean isProgress;
    @NotNull
    private Boolean isShowDisabled;
}