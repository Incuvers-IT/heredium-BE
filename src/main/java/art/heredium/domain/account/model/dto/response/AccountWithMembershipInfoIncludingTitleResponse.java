package art.heredium.domain.account.model.dto.response;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.membership.entity.PaymentStatus;

@Getter
@NoArgsConstructor
public class AccountWithMembershipInfoIncludingTitleResponse {

  @JsonProperty("membership_name")
  private String membershipName;

  @JsonProperty("title")
  private String title;

  @JsonProperty("payment_status")
  private String paymentStatus;

  @JsonProperty("payment_date")
  private LocalDate paymentDate;

  @JsonProperty("start_date")
  private LocalDate startDate;

  @JsonProperty("end_date")
  private LocalDate endDate;

  @JsonProperty("number_of_coupons")
  private Long numberOfUsedCoupons;

  @JsonProperty("email")
  private String email;

  @JsonProperty("name")
  private String name;

  @JsonProperty("phone")
  private String phone;

  @JsonProperty("amount")
  private Long amount;

  public AccountWithMembershipInfoIncludingTitleResponse(
      final String membershipName,
      final String title,
      final PaymentStatus paymentStatus,
      final LocalDate paymentDate,
      final LocalDate startDate,
      final LocalDate endDate,
      final Long numberOfUsedCoupons,
      final String email,
      final String name,
      final String phone,
      final Long amount) {
    this.membershipName = membershipName;
    this.title = title;
    this.paymentStatus = paymentStatus != null ? paymentStatus.getDesc() : null;
    this.paymentDate = paymentDate;
    this.startDate = startDate;
    this.endDate = endDate;
    this.numberOfUsedCoupons = numberOfUsedCoupons;
    this.email = email;
    this.name = name;
    this.phone = phone;
    this.amount = amount;
  }
}
