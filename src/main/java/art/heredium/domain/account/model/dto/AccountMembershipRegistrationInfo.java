package art.heredium.domain.account.model.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountMembershipRegistrationInfo {

  @JsonProperty("membership_registration_id")
  private Long membershipRegistrationId;

  @JsonProperty("registration_date")
  private LocalDate registrationDate;

  @JsonProperty("expiration_date")
  private LocalDate expirationDate;
}
