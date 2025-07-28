package art.heredium.domain.membership.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.membership.entity.PaymentStatus;

@Getter
@NoArgsConstructor
public class ActiveMembershipDetailResponse {

  @JsonProperty("membership_name")
  private String membershipOrCompanyName;

  @JsonProperty("account_id")
  private Long accountId;

  @JsonProperty("email")
  private String email;

  @JsonProperty("createdDate")
  private LocalDateTime createdDate;

  @JsonProperty("name")
  private String name;

  @JsonProperty("phone")
  private String phone;

  @JsonProperty("lastLoginDate")
  private LocalDateTime lastLoginDate;

  @JsonProperty("gender")
  private String gender;

  @JsonProperty("birthDate")
  private String birthDate;

  @JsonProperty("isMarketingReceive")
  private Boolean isMarketingReceive;

  @JsonProperty("payment_status")
  private String paymentStatus;

  @JsonProperty("registrationDate")
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

  @JsonProperty("code")
  private Integer code;

  public ActiveMembershipDetailResponse(
          final String membershipOrCompanyName,
          final Long accountId,
          final String email,
          final LocalDateTime createdDate,
          final String name,
          final String phone,
          final LocalDateTime lastLoginDate,
          final String gender,
          final String birthDate,
          final Boolean isMarketingReceive,
          final PaymentStatus paymentStatus,
          final LocalDateTime registrationDate,
          final Long numberOfMemberships,
          final Long numberOfExhibitionsUsed,
          final Long numberOfProgramsUsed,
          final Long numberOfCoffeeUsed,
          final Boolean isAgreeToReceiveMarketing,
          final Integer code
  ) {
    this.membershipOrCompanyName = membershipOrCompanyName;
    this.accountId = accountId;
    this.email = email;
    this.createdDate = createdDate;
    this.name = name;
    this.phone = phone;
    this.lastLoginDate = lastLoginDate;
    this.gender = gender;
    this.birthDate = birthDate;
    this.isMarketingReceive = isMarketingReceive;
    this.paymentStatus = paymentStatus != null ? paymentStatus.getDesc() : null;
    this.registrationDate = registrationDate;
    this.numberOfMemberships = numberOfMemberships;
    this.numberOfExhibitionsUsed = numberOfExhibitionsUsed;
    this.numberOfProgramsUsed = numberOfProgramsUsed;
    this.numberOfCoffeeUsed = numberOfCoffeeUsed;
    this.isAgreeToReceiveMarketing = isAgreeToReceiveMarketing;
    this.code = code;
  }
}