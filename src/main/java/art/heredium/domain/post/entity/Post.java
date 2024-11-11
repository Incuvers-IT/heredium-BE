package art.heredium.domain.post.entity;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.common.entity.BaseEntity;
import art.heredium.domain.membership.entity.Membership;

@Entity
@Getter
@Setter
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

  @Column(name = "image_original_file_name")
  private String imageOriginalFileName;

  @Column(name = "image_url")
  private String imageUrl;

  @Column(name = "thumbnail_urls")
  private String thumbnailUrls;

  @Column(name = "is_enabled", nullable = false)
  private Boolean isEnabled;

  @Column(name = "content_detail", columnDefinition = "TEXT")
  private String contentDetail;

  @Column(name = "sub_title", columnDefinition = "TEXT")
  private String subTitle;

  @Column(name = "future_exhibition_count")
  private Integer futureExhibitionCount;

  @Column(name = "ongoing_exhibition_count")
  private Integer ongoingExhibitionCount;

  @Column(name = "completed_exhibition_count")
  private Integer completedExhibitionCount;

  @Column(name = "future_program_count")
  private Integer futureProgramCount;

  @Column(name = "ongoing_program_count")
  private Integer ongoingProgramCount;

  @Column(name = "completed_program_count")
  private Integer completedProgramCount;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(name = "open_date")
  private LocalDate openDate;

  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id ASC")
  private List<Membership> memberships;

  @ManyToOne
  @JoinColumn(name = "admin_id", nullable = false)
  private Admin admin;

  public void updateIsEnabled(boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  public void updateThumbnailUrls(final String thumbnailUrls) {
    this.thumbnailUrls = thumbnailUrls;
  }

  public void updateImageUrl(final String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public void updateContentDetail(final String contentDetail) {
    this.contentDetail = contentDetail;
  }

  @Builder
  public Post(
      String name,
      String imageUrl,
      String imageOriginalFileName,
      String thumbnailUrls,
      Boolean isEnabled,
      String contentDetail,
      String subTitle,
      Admin admin,
      Integer futureExhibitionCount,
      Integer ongoingExhibitionCount,
      Integer completedExhibitionCount,
      Integer futureProgramCount,
      Integer ongoingProgramCount,
      Integer completedProgramCount,
      LocalDate startDate,
      LocalDate endDate,
      LocalDate openDate) {
    this.name = name;
    this.imageUrl = imageUrl;
    this.imageOriginalFileName = imageOriginalFileName;
    this.thumbnailUrls = thumbnailUrls;
    this.isEnabled = isEnabled;
    this.contentDetail = contentDetail;
    this.subTitle = subTitle;
    this.admin = admin;
    this.futureExhibitionCount = futureExhibitionCount;
    this.ongoingExhibitionCount = ongoingExhibitionCount;
    this.completedExhibitionCount = completedExhibitionCount;
    this.futureProgramCount = futureProgramCount;
    this.ongoingProgramCount = ongoingProgramCount;
    this.completedProgramCount = completedProgramCount;
    this.startDate = startDate;
    this.endDate = endDate;
    this.openDate = openDate;
  }
}
