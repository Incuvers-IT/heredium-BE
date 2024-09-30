package art.heredium.domain.slide.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class GetAdminSlideRequest {
    private String text;
    @NotNull
    private Boolean isProgress;
    @NotNull
    private Boolean isShowDisabled;
}