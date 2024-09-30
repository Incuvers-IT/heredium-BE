package art.heredium.domain.exhibition.model.dto.request;

import art.heredium.domain.common.type.ProjectStateType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class GetUserExhibitionRequest {
    @NotNull
    private List<ProjectStateType> states;
}