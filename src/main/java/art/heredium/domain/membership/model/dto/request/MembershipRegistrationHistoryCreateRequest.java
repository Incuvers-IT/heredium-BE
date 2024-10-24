package art.heredium.domain.membership.model.dto.request;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.account.entity.Account;
import art.heredium.domain.membership.entity.RegistrationStatus;

@Getter
@Setter
public class MembershipRegistrationHistoryCreateRequest {
  private String title;

  private String emailOrPhone;

  private LocalDate startDate;

  private Long price;

  private LocalDate paymentDate;

  private RegistrationStatus status;

  private String reason;

  private Account account;

  @Builder
  public MembershipRegistrationHistoryCreateRequest(
      final String title,
      final String emailOrPhone,
      final LocalDate startDate,
      final Long price,
      final LocalDate paymentDate,
      final RegistrationStatus status,
      final String reason,
      final Account account) {
    this.title = title;
    this.emailOrPhone = emailOrPhone;
    this.startDate = startDate;
    this.price = price;
    this.paymentDate = paymentDate;
    this.status = status;
    this.reason = reason;
    this.account = account;
  }
}
