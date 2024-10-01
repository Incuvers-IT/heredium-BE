package art.heredium.domain.common.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.model.dto.request.GetUserCommonSearchRequest;

@Getter
@Setter
public class GetUserCommonSearchIndexResponse {
  private GetUserCommonSearchRequest.SearchDateType type;
  private Long count;

  public GetUserCommonSearchIndexResponse(
      GetUserCommonSearchRequest.SearchDateType type, Long count) {
    this.type = type;
    this.count = count;
  }
}
