package art.heredium.domain.membership.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.membership.entity.PaymentStatus;

@Getter
@NoArgsConstructor
public class ActiveMembershipRegistrationsResponse {

  @JsonProperty("membership_name")
  private String membershipOrCompanyName;

  @JsonProperty("account_id")
  private Long accountId;

  @JsonProperty("email")
  private String email;

  @JsonProperty("name")
  private String name;

  @JsonProperty("phone")
  private String phone;

  @JsonProperty("payment_status")
  private String paymentStatus;

  @JsonProperty("registration_date")
  private LocalDateTime registrationDate;

  @JsonProperty("number_of_memberships")
  private Long numberOfMemberships;

  @JsonProperty("number_of_exhibitions_used")
  private Long numberOfExhibitionsUsed;

  @JsonProperty("number_of_programs_used")
  private Long numberOfProgramsUsed;

  @JsonProperty("number_of_coffee_used")
  private Long numberOfCoffeeUsed;

  @JsonProperty("is_agree_to_receive_marketing")
  private Boolean isAgreeToReceiveMarketing;

  @JsonProperty("mileage_sum")
  private Integer mileageSum;

  public ActiveMembershipRegistrationsResponse(
      final String membershipOrCompanyName,
      final Long accountId,
      final String email,
      final String name,
      final String phone,
      final PaymentStatus paymentStatus,
      final LocalDateTime registrationDate,
      final Long numberOfMemberships,
      final Long numberOfExhibitionsUsed,
      final Long numberOfProgramsUsed,
      final Long numberOfCoffeeUsed,
      final Boolean isAgreeToReceiveMarketing,
      final Integer mileageSum
      ) {
    this.membershipOrCompanyName = membershipOrCompanyName;
    this.accountId = accountId;
    this.email = email;
    this.name = name;
    this.phone = phone;
    this.paymentStatus = paymentStatus != null ? paymentStatus.getDesc() : null;
    this.registrationDate = registrationDate;
    this.numberOfMemberships = numberOfMemberships;
    this.numberOfExhibitionsUsed = numberOfExhibitionsUsed;
    this.numberOfProgramsUsed = numberOfProgramsUsed;
    this.numberOfCoffeeUsed = numberOfCoffeeUsed;
    this.isAgreeToReceiveMarketing = isAgreeToReceiveMarketing;
    this.mileageSum = mileageSum;
  }
}
