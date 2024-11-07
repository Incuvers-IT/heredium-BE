package art.heredium.domain.account.model.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class UploadCouponIssuanceTemplateResponse {

  @JsonProperty("coupon_issuance_accounts")
  private List<AccountWithMembershipInfoResponse> couponIssuanceAccounts;
}
