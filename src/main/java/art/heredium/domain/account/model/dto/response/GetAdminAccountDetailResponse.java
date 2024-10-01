package art.heredium.domain.account.model.dto.response;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.entity.AccountInfo;
import art.heredium.domain.account.entity.SleeperInfo;
import art.heredium.oauth.provider.OAuth2Provider;

@Getter
@Setter
public class GetAdminAccountDetailResponse {
  private Long id;
  private String email;
  private String name;
  private String phone;
  private LocalDateTime createdDate;
  private LocalDateTime lastLoginDate;
  private LocalDateTime sleepDate;
  private LocalDateTime terminateDate;
  private Boolean isMarketingReceive;
  private Boolean isLocalResident;
  private OAuth2Provider provider;

  public GetAdminAccountDetailResponse(Account entity) {
    AccountInfo accountInfo = entity.getAccountInfo();
    SleeperInfo sleeperInfo = entity.getSleeperInfo();
    this.id = entity.getId();
    this.email = entity.getEmail();
    this.createdDate = entity.getCreatedDate();
    this.provider = entity.getProviderType();
    if (accountInfo != null) {
      this.name = accountInfo.getName();
      this.phone = accountInfo.getPhone();
      this.lastLoginDate = accountInfo.getLastLoginDate();
      this.isMarketingReceive = accountInfo.getIsMarketingReceive();
      this.isLocalResident = accountInfo.getIsLocalResident();
    } else if (sleeperInfo != null) {
      this.name = sleeperInfo.getName();
      this.phone = sleeperInfo.getPhone();
      this.lastLoginDate = sleeperInfo.getLastLoginDate();
      this.isMarketingReceive = sleeperInfo.getIsMarketingReceive();
      this.isLocalResident = sleeperInfo.getIsLocalResident();
      this.sleepDate = sleeperInfo.getSleepDate();
      this.terminateDate = sleeperInfo.getSleepDate().plus(2, ChronoUnit.YEARS);
    }
  }
}
