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
import org.hibernate.annotations.*;

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

  @Comment("멤버십 코드 (1:BASIC, 2:UPGRADE, 3:STUDENT)")
  @Column(name = "code")
  private Integer code;

  @Comment("멤버십명")
  @Column(name = "name", nullable = false)
  private String name;

  @Comment("약칭")
  @Column(name = "short_name")
  private String shortName;

  @Comment("가능")
  @Column(name = "is_enabled", nullable = false)
  private Boolean isEnabled;

  @Comment("이미지 URL")
  @Column(name = "image_url")
  private String imageUrl;

  @Comment("삭제 여부")
  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted;

  @Comment("이용실적(최소 기준)")
  @Column(name = "usage_threshold", nullable = false)
  private int usageThreshold;

  public void updateIsEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  public void updateImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  /** 테이블에 DEFAULT(UUID()) 로 채워지는 멤버십 식별자 */
  @Comment("멤버십 UUID")
  @Column(name = "uuid",
          nullable = false,
          updatable = false,
          insertable = false,
          length = 255,
          columnDefinition = "VARCHAR(255) NOT NULL DEFAULT (UUID())")
  @Generated(GenerationTime.INSERT)
  private String uuid;

  @Builder
  public Membership(
          String name,
          String shortName,
          Integer code,
          Boolean isEnabled,
          String imageUrl,
          Post post,
          int usageThreshold) {
    this.name = name;
    this.shortName = shortName;
    this.code = code;
    this.isEnabled = isEnabled;
    this.imageUrl = imageUrl;
    this.post = post;
    this.isDeleted = false;
    this.usageThreshold = usageThreshold;
  }
}
