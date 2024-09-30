package art.heredium.domain.account.model.dto.response;

import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.entity.AccountInfo;
import art.heredium.oauth.provider.OAuth2Provider;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GetUserAccountResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private Boolean isLocalResident;
    private Boolean isMarketingReceive;
    private OAuth2Provider provider;

    public GetUserAccountResponse(Account entity) {
        this.id = entity.getId();
        this.email = entity.getEmail();
        this.provider = entity.getProviderType();
        AccountInfo accountInfo = entity.getAccountInfo();
        this.name = accountInfo.getName();
        this.phone = accountInfo.getPhone();
        this.isLocalResident = accountInfo.getIsLocalResident();
        this.isMarketingReceive = accountInfo.getIsMarketingReceive();
    }
}
