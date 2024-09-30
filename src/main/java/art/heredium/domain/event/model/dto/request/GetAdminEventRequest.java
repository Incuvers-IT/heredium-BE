package art.heredium.domain.event.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class GetAdminEventRequest {
    private String text;
    @NotNull
    private Boolean isProgress;
    @NotNull
    private Boolean isShowDisabled;
}