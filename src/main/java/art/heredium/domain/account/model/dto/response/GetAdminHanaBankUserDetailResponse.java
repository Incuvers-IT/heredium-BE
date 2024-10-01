package art.heredium.domain.account.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.account.entity.NonUser;

@Getter
@Setter
public class GetAdminHanaBankUserDetailResponse {
  private Long id;
  private String name;
  private String hanaBankUuid;

  public GetAdminHanaBankUserDetailResponse(NonUser entity) {
    this.id = entity.getId();
    this.name = entity.getName();
    this.hanaBankUuid = entity.getHanaBankUuid();
  }
}
