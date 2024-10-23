package art.heredium.domain.coupon.entity;

import java.time.LocalDateTime;

import javax.persistence.*;

import lombok.*;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import art.heredium.domain.common.entity.BaseEntity;
import art.heredium.domain.membership.entity.Membership;

@Entity
@Getter
@Setter
@Table(name = "coupon")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
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

  @Comment("멤버십 쿠폰이 아닙")
  @Column(name = "is_non_membership_coupon")
  private Boolean isNonMembershipCoupon;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "membership_id")
  private Membership membership;

  @Builder
  public Coupon(
      String name,
      CouponType couponType,
      Integer discountPercent,
      Integer periodInDays,
      String imageUrl,
      Membership membership,
      Long numberOfUses,
      Boolean isPermanent,
      Boolean isNonMembershipCoupon) {
    this.name = name;
    this.couponType = couponType;
    this.discountPercent = discountPercent;
    this.periodInDays = periodInDays;
    this.imageUrl = imageUrl;
    this.membership = membership;
    this.numberOfUses = numberOfUses;
    this.isPermanent = isPermanent;
    this.isNonMembershipCoupon = isNonMembershipCoupon;
  }

  public void updateImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }
}
