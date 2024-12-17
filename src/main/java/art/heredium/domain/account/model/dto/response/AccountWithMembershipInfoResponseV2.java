package art.heredium.domain.account.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.membership.entity.PaymentStatus;
import art.heredium.domain.membership.entity.RegistrationType;

@Getter
@NoArgsConstructor
public class AccountWithMembershipInfoResponseV2 {

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

  @JsonProperty("account_id")
  private Long accountId;

  @JsonProperty("registration_type")
  private RegistrationType registrationType;

  @JsonProperty("membership_registration_id")
  private Long membershipRegistrationId;

  @JsonProperty("is_refundable")
  private Boolean isRefundable;

  public AccountWithMembershipInfoResponseV2(
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
      final Long accountId,
      final RegistrationType registrationType,
      final Long membershipRegistrationId,
      final Boolean isRefundable) {
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
    this.accountId = accountId;
    this.registrationType = registrationType;
    this.membershipRegistrationId = membershipRegistrationId;
    this.isRefundable = isRefundable;
  }
}
