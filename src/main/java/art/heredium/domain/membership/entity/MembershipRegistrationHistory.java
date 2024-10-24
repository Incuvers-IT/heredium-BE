package art.heredium.domain.membership.entity;

import java.time.LocalDate;

import javax.persistence.*;

import lombok.*;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import art.heredium.domain.account.entity.Account;

@Entity
@Getter
@Table(name = "membership_registration_history")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class MembershipRegistrationHistory {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "title")
  private String title;

  @Column(name = "email_or_phone")
  private String emailOrPhone;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "price")
  private Long price;

  @Column(name = "payment_date")
  private LocalDate paymentDate;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private RegistrationStatus status;

  @Column(name = "reason")
  private String reason;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @Builder
  public MembershipRegistrationHistory(
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
