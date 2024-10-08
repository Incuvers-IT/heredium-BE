package art.heredium.domain.coupon.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import org.springframework.lang.Nullable;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;

import art.heredium.domain.account.entity.Account;
import art.heredium.domain.common.entity.BaseEntity;

@Entity
@Getter
@Setter
@Table(name = "coupon_usage")
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(exclude = {"coupon", "account"})
public class CouponUsage extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "coupon_type_id", nullable = false)
  private Coupon coupon;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @Comment("사용 여부")
  @Column(name = "is_used", nullable = false)
  private Boolean isUsed;

  @Comment("발급 일자")
  @Column(name = "delivered_date", nullable = false)
  private LocalDateTime deliveredDate;

  @Comment("사용 일자")
  @Column(name = "used_date")
  private LocalDateTime usedDate;

  @Comment("만료 일자")
  @Column(name = "expiration_date", nullable = false)
  private LocalDateTime expirationDate;

  @Comment("쿠폰 uuid")
  @Column(name = "uuid", nullable = false, length = 36, unique = true, updatable = false)
  private String uuid;

  @Comment("사용된 횟수")
  @Column(name = "used_count")
  private Long usedCount;

  @Comment("상시할인")
  @Column(name = "is_permanent")
  private boolean isPermanent;

  public CouponUsage(
      @NonNull Coupon coupon,
      @NonNull Account account,
      @NonNull LocalDateTime deliveredDate,
      @NonNull LocalDateTime expirationDate,
      boolean isPermanent,
      @Nullable Long usedCount) {
    this.coupon = coupon;
    this.account = account;
    this.deliveredDate = deliveredDate;
    this.expirationDate = expirationDate;
    this.uuid = UUID.randomUUID().toString();
    this.isUsed = false;
    this.isPermanent = isPermanent;
    this.usedCount = usedCount;
  }
}
