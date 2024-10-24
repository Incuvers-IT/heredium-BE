package art.heredium.domain.account.model.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.membership.entity.PaymentStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountWithMembershipInfoIncludingTitleResponse {

  @JsonProperty("membership_name")
  private String membershipName;

  @JsonProperty("title")
  private String title;

  @JsonProperty("payment_status")
  private PaymentStatus paymentStatus;

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
}
