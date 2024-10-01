package art.heredium.domain.coupon.entity;

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
  @Column(name = "discount_percent", nullable = false)
  private Integer discountPercent;
}
