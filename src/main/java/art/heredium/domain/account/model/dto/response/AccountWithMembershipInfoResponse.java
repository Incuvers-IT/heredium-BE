package art.heredium.domain.account.model.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import art.heredium.domain.account.model.dto.AccountMembershipRegistrationInfo;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountWithMembershipInfoResponse {
  private Long id;
  private String email;
  private String name;
  private String phone;
  private LocalDateTime createdDate;
  private LocalDateTime lastLoginDate;
  private Boolean hasEntries;
  private Boolean hasUsedCoupon;
  private String membershipName;
  private Long numberOfEntries;
  private AccountMembershipRegistrationInfo membershipRegistrationInfo;
}
