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

  @JsonProperty("title")
  private String title;

  @JsonProperty("payment_status")
  private String paymentStatus;

  @JsonProperty("payment_date")
  private LocalDateTime paymentDate;

  @JsonProperty("start_date")
  private LocalDateTime startDate;

  @JsonProperty("end_date")
  private LocalDateTime endDate;

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

  @JsonProperty("created_date")
  private LocalDateTime createdDate;

  @JsonProperty("last_login_date")
  private LocalDateTime lastLoginDate;

  @JsonProperty("usage_count")
  private String usageCount;

  @JsonProperty("marketing_consent")
  private Boolean marketingConsent;

  public AccountWithMembershipInfoExcelDownloadResponse(
      final String membershipName,
      final String title,
      final PaymentStatus paymentStatus,
      final LocalDateTime paymentDate,
      final LocalDateTime startDate,
      final LocalDateTime endDate,
      final Long numberOfUsedCoupons,
      final String email,
      final String name,
      final String phone,
      final Long amount,
      final LocalDateTime createdDate,
      final LocalDateTime lastLoginDate,
      final String usageCount,
      final Boolean marketingConsent) {
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
    this.createdDate = createdDate;
    this.lastLoginDate = lastLoginDate;
    this.usageCount = usageCount;
    this.marketingConsent = marketingConsent;
  }
}
