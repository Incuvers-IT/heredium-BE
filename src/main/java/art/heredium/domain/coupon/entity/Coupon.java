package art.heredium.domain.coupon.entity;

import java.time.LocalDateTime;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import art.heredium.domain.common.entity.BaseEntity;
import art.heredium.domain.membership.entity.Membership;

@Entity
@Getter
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "membership_id")
  private Membership membership;
}