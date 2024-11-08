package art.heredium.domain.membership.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.Min;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import art.heredium.domain.common.entity.BaseEntity;
import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.post.entity.Post;

@Entity
@Getter
@Setter
@Table(name = "membership")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
// 멤버십
public class Membership extends BaseEntity implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @OneToMany(mappedBy = "membership", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("name ASC")
  private List<Coupon> coupons;

  @Comment("멤버십명")
  @Column(name = "name", nullable = false)
  private String name;

  @Comment("멤버십기간 (일)")
  @Column(name = "membership_period", nullable = false)
  @Min(1)
  private Long period;

  @Comment("가격")
  @Column(name = "price", nullable = false)
  @Min(1)
  private Integer price;

  @Comment("가능")
  @Column(name = "is_enabled", nullable = false)
  private Boolean isEnabled;

  @Comment("이미지 URL")
  @Column(name = "image_url")
  private String imageUrl;

  @Comment("가입하기 버튼 활성화 여부")
  @Column(name = "is_register_membership_button_shown", nullable = false)
  private Boolean isRegisterMembershipButtonShown;

  @Comment("삭제 여부")
  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted;

  public void updateIsEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  public void updateImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  @Builder
  public Membership(
      String name,
      Long period,
      Integer price,
      Boolean isEnabled,
      String imageUrl,
      Post post,
      Boolean isRegisterMembershipButtonShown) {
    this.name = name;
    this.period = period;
    this.price = price;
    this.isEnabled = isEnabled;
    this.imageUrl = imageUrl;
    this.post = post;
    this.isRegisterMembershipButtonShown = isRegisterMembershipButtonShown;
    this.isDeleted = false;
  }
}
