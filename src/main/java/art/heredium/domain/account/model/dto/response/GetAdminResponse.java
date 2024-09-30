package art.heredium.domain.account.model.dto.response;

import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.account.entity.AdminInfo;
import art.heredium.domain.account.type.AuthType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetAdminResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private LocalDateTime createdDate;
    private LocalDateTime lastLoginDate;
    private AuthType auth;
    private Boolean isEnabled;

    public GetAdminResponse(Admin entity) {
        AdminInfo adminInfo = entity.getAdminInfo();
        this.id = adminInfo.getId();
        this.email = entity.getEmail();
        this.name = adminInfo.getName();
        this.phone = adminInfo.getPhone();
        this.createdDate = adminInfo.getCreatedDate();
        this.lastLoginDate = adminInfo.getLastLoginDate();
        this.auth = adminInfo.getAuth();
        this.isEnabled = adminInfo.getIsEnabled();
    }
}
