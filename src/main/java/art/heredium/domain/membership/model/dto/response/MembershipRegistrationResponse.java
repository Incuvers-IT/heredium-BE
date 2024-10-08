package art.heredium.domain.membership.model.dto.response;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.membership.entity.MembershipRegistration;

@Getter
@Setter
public class MembershipRegistrationResponse {

  @JsonProperty("id")
  private long membershipRegistrationId;

  @JsonProperty("uuid")
  private String uuid;

  @JsonProperty("membership_name")
  private String membershipName;

  @JsonProperty("registration_date")
  private LocalDate registrationDate;

  @JsonProperty("expiration_date")
  private LocalDate expirationDate;

  public MembershipRegistrationResponse(@NonNull MembershipRegistration membershipRegistration) {
    this.membershipName = membershipRegistration.getMembership().getName();
    this.registrationDate = membershipRegistration.getRegistrationDate();
    this.expirationDate = membershipRegistration.getExpirationDate();
    this.membershipRegistrationId = membershipRegistration.getId();
    this.uuid = membershipRegistration.getUuid();
  }
}
