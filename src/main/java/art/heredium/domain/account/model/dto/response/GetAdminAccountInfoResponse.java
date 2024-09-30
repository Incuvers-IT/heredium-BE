package art.heredium.domain.account.model.dto.response;

import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.account.type.AuthType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAdminAccountInfoResponse {
    private Long id;
    private AuthType auth;
    private String email;
    private String phone;
    private String name;

    public GetAdminAccountInfoResponse(Admin entity) {
        this.id = entity.getId();
        this.auth = entity.getAdminInfo().getAuth();
        this.email = entity.getEmail();
        this.phone = entity.getAdminInfo().getPhone();
        this.name = entity.getAdminInfo().getName();
    }
}
