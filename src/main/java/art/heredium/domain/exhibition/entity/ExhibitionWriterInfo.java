package art.heredium.domain.exhibition.entity;

import java.io.Serializable;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.exhibition.model.dto.request.PostAdminExhibitionRequest;

@Entity
@Getter
@Table(name = "exhibition_writer_info")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"exhibitionWriter"})
// 전시 인원 정보
public class ExhibitionWriterInfo implements Serializable {
  private static final long serialVersionUID = 4922467054075409408L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exhibition_writer_id", nullable = false)
  private ExhibitionWriter exhibitionWriter;

  @Comment("썸네일")
  @Type(type = "json")
  @Column(name = "thumbnail", columnDefinition = "json")
  private Storage thumbnail;

  @Comment("이름")
  @Column(name = "name", nullable = false, length = 30)
  private String name;

  @Comment("소개")
  @Column(name = "intro", nullable = false, columnDefinition = "TEXT")
  private String intro;

  @Comment("순서")
  @Column(name = "orders", nullable = false)
  private Long order;

  public ExhibitionWriterInfo(
      PostAdminExhibitionRequest.Writer.WriterInfo dto,
      ExhibitionWriter exhibitionWriter,
      long order) {
    this.exhibitionWriter = exhibitionWriter;
    this.thumbnail = dto.getThumbnail();
    this.name = dto.getName();
    this.intro = dto.getIntro();
    this.order = order;
  }

  public void update(PostAdminExhibitionRequest.Writer.WriterInfo dto, long order) {
    this.thumbnail = dto.getThumbnail();
    this.name = dto.getName();
    this.intro = dto.getIntro();
    this.order = order;
  }
}
