package art.heredium.domain.common.model.dto.response;

import art.heredium.domain.common.model.dto.request.GetUserCommonSearchRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserCommonSearchIndexResponse {
    private GetUserCommonSearchRequest.SearchDateType type;
    private Long count;

    public GetUserCommonSearchIndexResponse(GetUserCommonSearchRequest.SearchDateType type, Long count) {
        this.type = type;
        this.count = count;
    }
}