package art.heredium.domain.membership.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.membership.entity.MembershipRegistration;

@Getter
@Setter
public class RegisterMembershipResponse {
  @JsonProperty("membership_registration_id")
  private long membershipRegistrationId;

  public RegisterMembershipResponse(final MembershipRegistration membershipRegistration) {
    this.membershipRegistrationId = membershipRegistration.getId();
//    this.amount = membershipRegistration.getMembership().getPrice();
//    this.paymentOrderId = membershipRegistration.getPaymentOrderId();
  }
}
