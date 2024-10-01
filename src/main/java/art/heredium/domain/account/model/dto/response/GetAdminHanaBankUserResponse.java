package art.heredium.domain.account.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import com.querydsl.core.annotations.QueryProjection;

@Getter
@Setter
public class GetAdminHanaBankUserResponse {
  private Long id;
  private String hanaBankUuid;
  private String name;
  private Long visitCount;

  @QueryProjection
  public GetAdminHanaBankUserResponse(Long id, String name, String hanaBankUuid, Long visitCount) {
    this.id = id;
    this.name = name;
    this.hanaBankUuid = hanaBankUuid;
    this.visitCount = visitCount;
  }
}
