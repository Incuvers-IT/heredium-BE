package art.heredium.domain.membership.entity;

import javax.persistence.*;

import lombok.*;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import art.heredium.domain.account.entity.Account;

@Entity
@Getter
@Table(name = "company_membership_registration_history")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class CompanyMembershipRegistrationHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "email")
  private String email;

  @Column(name = "phone")
  private String phone;

  @Column(name = "start_date")
  private String startDate;

  @Column(name = "price")
  private String price;

  @Column(name = "payment_date")
  private String paymentDate;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private RegistrationStatus status;

  @Column(name = "failed_reason")
  private String failedReason;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  private Account account;

  @Builder
  public CompanyMembershipRegistrationHistory(
      final String email,
      final String phone,
      final String startDate,
      final String price,
      final String paymentDate,
      final RegistrationStatus status,
      final String failedReason,
      final Account account) {
    this.email = email;
    this.phone = phone;
    this.startDate = startDate;
    this.price = price;
    this.paymentDate = paymentDate;
    this.status = status;
    this.failedReason = failedReason;
    this.account = account;
  }

  public void updateStatus(RegistrationStatus status) {
    this.status = status;
  }
}
