package art.heredium.domain.coupon.entity;

import art.heredium.domain.common.entity.BaseEntity;
import art.heredium.domain.company.entity.Company;
import art.heredium.domain.membership.entity.Membership;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "coupon")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
@Where(clause = "is_deleted = false")
public class Coupon extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Comment("쿠폰 이름")
  @Column(name = "name", nullable = false)
  private String name;

  @Comment("쿠폰 타입")
  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private CouponType couponType;

  @Comment("할인 퍼센트")
  @Column(name = "discount_percent")
  private Integer discountPercent;

  @Comment("유효 기간 (일)")
  @Column(name = "period_in_days")
  private Integer periodInDays;

  @Comment("시작 날짜")
  @Column(name = "started_date")
  private LocalDateTime startedDate;

  @Comment("종료 날짜")
  @Column(name = "ended_date")
  private LocalDateTime endedDate;

  @Comment("이미지 URL")
  @Column(name = "image_url")
  private String imageUrl;

  @Comment("사용횟수")
  @Column(name = "number_of_uses", nullable = true)
  private Long numberOfUses;

  @Comment("상시할인")
  @Column(name = "is_permanent")
  private Boolean isPermanent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "membership_id")
  private Membership membership;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id")
  private Company company;

  @Comment("원천")
  @Enumerated(EnumType.STRING)
  @Column(name = "from_source", nullable = false)
  private CouponSource fromSource;

  @Column(name = "is_deleted", nullable = false)
  private boolean isDeleted;

  @Column(name = "is_recurring")
  private Boolean isRecurring;

  @Type(type = "json")
  @Column(name = "recipient_type", columnDefinition = "json")
  private List<Short> recipientType;

  @Column(name = "send_day_of_month")
  private Integer sendDayOfMonth;

  @Column(name = "marketing_consent_benefit")
  private Boolean marketingConsentBenefit;

  @Builder
  public Coupon(
      String name,
      CouponType couponType,
      Integer discountPercent,
      LocalDateTime startedDate,
      LocalDateTime endedDate,
      Integer periodInDays,
      String imageUrl,
      Membership membership,
      Company company,
      Long numberOfUses,
      Boolean isPermanent,
      CouponSource fromSource,
      Boolean isRecurring,
      List<Short> recipientType,
      Integer sendDayOfMonth,
      Boolean marketingConsentBenefit
      ) {
    this.name = name;
    this.couponType = couponType;
    this.discountPercent = discountPercent;
    this.startedDate = startedDate;
    this.endedDate = endedDate;
    this.periodInDays = periodInDays;
    this.imageUrl = imageUrl;
    this.membership = membership;
    this.company = company;
    this.numberOfUses = numberOfUses;
    this.isPermanent = isPermanent;
    this.fromSource = fromSource;
    this.isRecurring = isRecurring;
    this.recipientType = recipientType;
    this.sendDayOfMonth = sendDayOfMonth;
    this.marketingConsentBenefit = marketingConsentBenefit;
    this.isDeleted = false;
  }

  public void updateImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public void markDeleted() {
    this.isDeleted = true;
  }
}
