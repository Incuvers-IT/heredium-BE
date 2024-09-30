package art.heredium.domain.account.model.dto.response;

import art.heredium.domain.account.entity.NonUser;
import lombok.Getter;
import lombok.Setter;

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