package art.heredium.domain.membership.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.*;

import lombok.*;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import art.heredium.domain.account.entity.Account;
import art.heredium.domain.common.entity.BaseEntity;
import art.heredium.domain.company.entity.Company;
import org.hibernate.annotations.Where;

@Entity
@Setter
@Getter
@Table(name = "membership_registration")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Where(clause = "is_deleted = 0")
// 멤버십등록
public class MembershipRegistration extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Comment("티켓 uuid")
  @Column(name = "uuid", nullable = false, length = 36, unique = true, updatable = false)
  private String uuid;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "membership_id")
  private Membership membership;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id")
  private Company company;

  @Comment("가입일시")
  @Column(name = "registration_date")
  private LocalDateTime registrationDate;

  @Comment("만료일시")
  @Column(name = "expiration_date")
  private LocalDateTime expirationDate;

  @Comment("결제 상태")
  @Column(name = "payment_status")
  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  @Comment("등록 유형")
  @Column(name = "registration_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private RegistrationType registrationType;

  @Comment("등록자")
  @Column(name = "created_name")
  private String createdName;

  @Comment("수정자")
  @Column(name = "last_modified_name")
  private String lastModifiedName;

  @Comment("삭제여부")
  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted = false;

  public MembershipRegistration(
      @NonNull Account account,
      @NonNull Company company,
      @NonNull LocalDateTime registrationDate,
      @NonNull LocalDateTime expirationDate,
      @NonNull PaymentStatus paymentStatus,
      @NonNull RegistrationType registrationType) {
    this.account = account;
    this.company = company;
    this.registrationDate = registrationDate;
    this.expirationDate = expirationDate;
    this.paymentStatus = paymentStatus;
    this.registrationType = registrationType;
    this.uuid = UUID.randomUUID().toString();
  }

  public MembershipRegistration(
      @NonNull Account account,
      @NonNull Membership membership,
      @NonNull RegistrationType registrationType,
      @NonNull PaymentStatus paymentStatus) {
    this.uuid = UUID.randomUUID().toString();
    this.account = account;
    this.membership = membership;
    this.registrationType = registrationType;
    this.paymentStatus = paymentStatus;
  }

  public MembershipRegistration(
          @NonNull Account account,
          @NonNull Membership membership,
          @NonNull LocalDateTime registrationDate,
          @NonNull RegistrationType registrationType,
          @NonNull PaymentStatus paymentStatus,
          String createdName,
          String lastModifiedName) {
    this.uuid = UUID.randomUUID().toString();
    this.account = account;
    this.membership = membership;
    this.registrationDate = registrationDate;
    this.registrationType = registrationType;
    this.paymentStatus = paymentStatus;
    this.createdName = createdName;
    this.lastModifiedName = lastModifiedName;
  }

  public MembershipRegistration(
      @NonNull Account account,
      @NonNull Membership membership,
      @NonNull LocalDateTime registrationDate,
      @NonNull LocalDateTime expirationDate,
      @NonNull RegistrationType registrationType,
      @NonNull PaymentStatus paymentStatus
  ) {
    this.uuid = UUID.randomUUID().toString();
    this.account = account;
    this.membership = membership;
    this.registrationDate = registrationDate;
    this.expirationDate = expirationDate;
    this.registrationType = registrationType;
    this.paymentStatus = paymentStatus;
  }

  public MembershipRegistration(
      @NonNull Account account,
      @NonNull Company company,
      @NonNull LocalDateTime registrationDate,
      @NonNull LocalDateTime expirationDate,
      @NonNull RegistrationType registrationType) {
    this.uuid = UUID.randomUUID().toString();
    this.account = account;
    this.company = company;
    this.registrationDate = registrationDate;
    this.expirationDate = expirationDate;
    this.registrationType = registrationType;
  }

  public void updateRegistrationDate(LocalDateTime registrationDate) {
    this.registrationDate = registrationDate;
  }

  public void updateExpirationDate(LocalDateTime expirationDate) {
    this.expirationDate = expirationDate;
  }

  public void updatePaymentStatus(PaymentStatus paymentStatus) {
    this.paymentStatus = paymentStatus;
  }

  public String getMembershipOrCompanyName() {
    switch (registrationType) {
      case COMPANY:
        return this.company.getName();
      case MEMBERSHIP_PACKAGE:
        return this.membership.getName();
      default:
        return null;
    }
  }
}
