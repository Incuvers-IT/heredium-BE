package art.heredium.domain.account.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.membership.entity.PaymentStatus;

@Getter
@NoArgsConstructor
public class AccountWithMembershipInfoExcelDownloadResponse {

  @JsonProperty("membership_name")
  private String membershipName;

  @JsonProperty("payment_status")
  private String paymentStatus;

  @JsonProperty("payment_date")
  private LocalDateTime paymentDate;

  @JsonProperty("start_date")
  private LocalDateTime startDate;

  @JsonProperty("end_date")
  private LocalDateTime endDate;

  @JsonProperty("number_of_used_exhibition_coupons")
  private Long numberOfUsedExhibitionCoupons;

  @JsonProperty("number_of_used_program_coupons")
  private Long numberOfUsedProgramCoupons;

  @JsonProperty("number_of_used_coffee_coupons")
  private Long numberOfUsedCoffeeCoupons;

  @JsonProperty("email")
  private String email;

  @JsonProperty("name")
  private String name;

  @JsonProperty("phone")
  private String phone;

  @JsonProperty("amount")
  private Long amount;

  @JsonProperty("created_date")
  private LocalDateTime createdDate;

  @JsonProperty("last_login_date")
  private LocalDateTime lastLoginDate;

  @JsonProperty("number_of_active_memberships")
  private Long numberOfActiveMemberships;

  @JsonProperty("number_of_exhibition_tickets")
  private Integer numberOfExhibitionTickets;

  @JsonProperty("number_of_program_tickets")
  private Integer numberOfProgramTickets;

  @JsonProperty("number_of_coffee_tickets")
  private Integer numberOfCoffeeTickets;

  @JsonProperty("marketing_consent")
  private Boolean marketingConsent;

  public AccountWithMembershipInfoExcelDownloadResponse(
      final String membershipName,
      final PaymentStatus paymentStatus,
      final LocalDateTime paymentDate,
      final LocalDateTime startDate,
      final LocalDateTime endDate,
      final Long numberOfUsedExhibitionCoupons,
      final Long numberOfUsedProgramCoupons,
      final Long numberOfUsedCoffeeCoupons,
      final String email,
      final String name,
      final String phone,
      final Long amount,
      final LocalDateTime createdDate,
      final LocalDateTime lastLoginDate,
      final Long numberOfActiveMemberships,
      final Integer numberOfExhibitionTickets,
      final Integer numberOfProgramTickets,
      final Integer numberOfCoffeeTickets,
      final Boolean marketingConsent) {
    this.membershipName = membershipName;
    this.paymentStatus = paymentStatus != null ? paymentStatus.getDesc() : null;
    this.paymentDate = paymentDate;
    this.startDate = startDate;
    this.endDate = endDate;
    this.numberOfUsedExhibitionCoupons = numberOfUsedExhibitionCoupons;
    this.numberOfUsedProgramCoupons = numberOfUsedProgramCoupons;
    this.numberOfUsedCoffeeCoupons = numberOfUsedCoffeeCoupons;
    this.email = email;
    this.name = name;
    this.phone = phone;
    this.amount = amount;
    this.createdDate = createdDate;
    this.lastLoginDate = lastLoginDate;
    this.numberOfActiveMemberships = numberOfActiveMemberships;
    this.numberOfExhibitionTickets = numberOfExhibitionTickets;
    this.numberOfProgramTickets = numberOfProgramTickets;
    this.numberOfCoffeeTickets = numberOfCoffeeTickets;
    this.marketingConsent = marketingConsent;
  }
}
