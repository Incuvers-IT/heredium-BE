package art.heredium.domain.membership.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import art.heredium.domain.account.entity.Account;
import art.heredium.domain.common.entity.BaseEntity;
import art.heredium.domain.company.entity.Company;
import art.heredium.payment.type.PaymentType;

@Entity
@Getter
@Table(name = "membership_registration")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
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

  @Column(name = "payment_date")
  private LocalDateTime paymentDate;

  @Column(name = "payment_order_id", length = 36, unique = true, updatable = false)
  private String paymentOrderId;

  @Column(name = "payment_key", unique = true)
  private String paymentKey;

  @Column(name = "payment_type")
  @Convert(converter = PaymentType.Converter.class)
  private PaymentType paymentType;

  @Comment("등록 유형")
  @Column(name = "registration_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private RegistrationType registrationType;

  @Comment("가격")
  @Column(name = "price")
  private Long price;

  public MembershipRegistration(
      @NonNull Account account,
      @NonNull Company company,
      @NonNull LocalDateTime registrationDate,
      @NonNull LocalDateTime expirationDate,
      @NonNull PaymentStatus paymentStatus,
      @NonNull LocalDateTime paymentDate,
      @NonNull RegistrationType registrationType,
      @NonNull Long price) {
    this.account = account;
    this.company = company;
    this.registrationDate = registrationDate;
    this.expirationDate = expirationDate;
    this.paymentStatus = paymentStatus;
    this.paymentDate = paymentDate;
    this.registrationType = registrationType;
    this.price = price;
    this.uuid = UUID.randomUUID().toString();
  }

  public MembershipRegistration(
      @NonNull Account account,
      @NonNull Membership membership,
      @NonNull RegistrationType registrationType,
      @NonNull PaymentStatus paymentStatus,
      @NonNull String paymentOrderId) {
    this.uuid = UUID.randomUUID().toString();
    this.account = account;
    this.membership = membership;
    this.registrationType = registrationType;
    this.paymentStatus = paymentStatus;
    this.paymentOrderId = paymentOrderId;
  }

  public MembershipRegistration(
      @NonNull Account account,
      @NonNull Membership membership,
      @NonNull LocalDateTime registrationDate,
      @NonNull LocalDateTime expirationDate,
      @NonNull RegistrationType registrationType,
      @NonNull PaymentStatus paymentStatus,
      @NonNull LocalDateTime paymentDate,
      @NonNull String paymentOrderId) {
    this.uuid = UUID.randomUUID().toString();
    this.account = account;
    this.membership = membership;
    this.registrationDate = registrationDate;
    this.expirationDate = expirationDate;
    this.registrationType = registrationType;
    this.paymentStatus = paymentStatus;
    this.paymentDate = paymentDate;
    this.paymentOrderId = paymentOrderId;
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
    // TODO: Add paymentStatus and paymentDate
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

  public void updatePaymentDate(LocalDateTime paymentDate) {
    this.paymentDate = paymentDate;
  }

  public void updatePaymentKey(String paymentKey) {
    this.paymentKey = paymentKey;
  }

  public void updatePaymentType(PaymentType paymentType) {
    this.paymentType = paymentType;
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
