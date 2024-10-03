package art.heredium.domain.membership.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import art.heredium.domain.common.entity.BaseEntity;
import art.heredium.domain.coupon.entity.Coupon;

@Entity
@Getter
@Table(name = "membership")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
// 멤버십
public class Membership extends BaseEntity implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToMany(mappedBy = "membership", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("order ASC")
  private List<Coupon> coupons;

  @Comment("멤버십명")
  @Column(name = "name", nullable = false)
  private String name;

  @Comment("멤버십기간 (날)")
  @Column(name = "membership_period", nullable = false)
  private Long period;

  @Comment("가격")
  @Column(name = "price", nullable = false)
  private Integer price;

  @Comment("이미지 URL")
  @Column(name = "image_url", nullable = false)
  private String imageUrl;

  @Comment("가능")
  @Column(name = "enabled", nullable = false)
  private Boolean enabled;
}
