package art.heredium.domain.program.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.program.entity.Program;

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
