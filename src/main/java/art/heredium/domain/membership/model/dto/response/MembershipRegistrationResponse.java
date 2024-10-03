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

  @JsonProperty("membership_name")
  private String membershipName;

  @JsonProperty("registration_date")
  private LocalDate registrationDate;

  @JsonProperty("expiration_date")
  private LocalDate expirationDate;

  @JsonProperty("image_url")
  private String imageUrl;

  public MembershipRegistrationResponse(@NonNull MembershipRegistration membershipRegistration) {
    this.membershipName = membershipRegistration.getMembership().getName();
    this.registrationDate = membershipRegistration.getRegistrationDate();
    this.expirationDate = membershipRegistration.getExpirationDate();
    this.imageUrl = membershipRegistration.getMembership().getImageUrl();
  }
}
