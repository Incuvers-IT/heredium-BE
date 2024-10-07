package art.heredium.domain.post.entity;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import art.heredium.domain.common.entity.BaseEntity;
import art.heredium.domain.membership.entity.Membership;

@Entity
@Getter
@Table(name = "post")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class Post extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "image_url", nullable = false)
  private String imageUrl;

  @Column(name = "is_enabled", nullable = false)
  private Boolean isEnabled;

  @Column(name = "content_detail", columnDefinition = "TEXT")
  private String contentDetail;

  @Column(name = "navigation_link", nullable = false)
  private String navigationLink;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("order ASC")
  private List<Membership> memberships;

  public void updateIsEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  @Builder
  public Post(
      String name,
      String imageUrl,
      Boolean isEnabled,
      String contentDetail,
      String navigationLink) {
    this.name = name;
    this.imageUrl = imageUrl;
    this.isEnabled = isEnabled;
    this.contentDetail = contentDetail;
    this.navigationLink = navigationLink;
  }
}
