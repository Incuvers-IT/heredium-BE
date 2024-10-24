package art.heredium.domain.account.model.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAccountWithMembershipInfoRequest {
  private LocalDateTime signUpDateFrom;
  private LocalDateTime signUpDateTo;
  private Boolean hasNumberOfEntries;
  private Boolean alreadyLoginedBefore;
  private Boolean alreadyUsedCouponBefore;
  private Boolean hasMembership;
  private String text;
  private List<Long> excludeIds;
}
