package art.heredium.domain.program.model.dto.response;

import art.heredium.domain.program.entity.Program;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAdminProgramExposeTitleResponse {
    private Long id;
    private String title;

    public GetAdminProgramExposeTitleResponse(Program entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
    }
}