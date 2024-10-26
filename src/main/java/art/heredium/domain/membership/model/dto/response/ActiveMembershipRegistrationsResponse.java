package art.heredium.domain.membership.model.dto.response;

import java.time.LocalDate;

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

  @JsonProperty("name")
  private String name;

  @JsonProperty("phone")
  private String phone;

  @JsonProperty("payment_status")
  private String paymentStatus;

  @JsonProperty("payment_date")
  private LocalDate paymentDate;

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

  public ActiveMembershipRegistrationsResponse(
      final String membershipOrCompanyName,
      final Long accountId,
      final String name,
      final String phone,
      final PaymentStatus paymentStatus,
      final LocalDate paymentDate,
      final Long numberOfMemberships,
      final Long numberOfExhibitionsUsed,
      final Long numberOfProgramsUsed,
      final Long numberOfCoffeeUsed,
      final Boolean isAgreeToReceiveMarketing) {
    this.membershipOrCompanyName = membershipOrCompanyName;
    this.accountId = accountId;
    this.name = name;
    this.phone = phone;
    this.paymentStatus = paymentStatus != null ? paymentStatus.getDesc() : null;
    this.paymentDate = paymentDate;
    this.numberOfMemberships = numberOfMemberships;
    this.numberOfExhibitionsUsed = numberOfExhibitionsUsed;
    this.numberOfProgramsUsed = numberOfProgramsUsed;
    this.numberOfCoffeeUsed = numberOfCoffeeUsed;
    this.isAgreeToReceiveMarketing = isAgreeToReceiveMarketing;
  }
}
