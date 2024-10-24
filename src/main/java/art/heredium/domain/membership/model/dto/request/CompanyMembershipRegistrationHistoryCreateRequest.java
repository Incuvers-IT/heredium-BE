package art.heredium.domain.membership.model.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.account.entity.Account;
import art.heredium.domain.membership.entity.RegistrationStatus;

@Getter
@Setter
public class CompanyMembershipRegistrationHistoryCreateRequest {
  private String title;

  private String email;

  private String phone;

  private String startDate;

  private String price;

  private String paymentDate;

  private RegistrationStatus status;

  private String failedReason;

  private Account account;

  @Builder
  public CompanyMembershipRegistrationHistoryCreateRequest(
      final String title,
      final String email,
      final String phone,
      final String startDate,
      final String price,
      final String paymentDate,
      final RegistrationStatus status,
      final String failedReason,
      final Account account) {
    this.title = title;
    this.email = email;
    this.phone = phone;
    this.startDate = startDate;
    this.price = price;
    this.paymentDate = paymentDate;
    this.status = status;
    this.failedReason = failedReason;
    this.account = account;
  }
}
