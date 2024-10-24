package art.heredium.domain.coupon.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;

import art.heredium.core.config.properties.HerediumProperties;
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
  private long usedCount;

  @Comment("상시할인")
  @Column(name = "is_permanent")
  private boolean isPermanent;

  public CouponUsage(
      @NonNull Coupon coupon,
      @NonNull Account account,
      @NonNull LocalDateTime deliveredDate,
      @NonNull LocalDateTime expirationDate,
      boolean isPermanent,
      long usedCount) {
    this.coupon = coupon;
    this.account = account;
    this.deliveredDate = deliveredDate;
    this.expirationDate = expirationDate;
    this.uuid = UUID.randomUUID().toString();
    this.isUsed = false;
    this.isPermanent = isPermanent;
    this.usedCount = usedCount;
  }

  public Map<String, String> getCouponUsageParams(
      @NonNull final HerediumProperties herediumProperties) {
    Map<String, String> params = new HashMap<>();
    params.put("coupon_name", coupon.getName());
    params.put("discount_percent", String.valueOf(coupon.getDiscountPercent()));
    if (usedDate != null) {
      params.put("used_date", usedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }
    if (!isPermanent) {
      params.put("used_count", String.valueOf(usedCount));
    }
    params.put("CSTel", herediumProperties.getTel());
    params.put("CSEmail", herediumProperties.getEmail());
    return params;
  }
}
