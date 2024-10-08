package art.heredium.domain.program.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import art.heredium.domain.program.model.dto.request.PostAdminProgramRequest;

@Entity
@Getter
@Table(name = "program_writer")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"program"})
// 프로그램 인원 소개
public class ProgramWriter implements Serializable {
  private static final long serialVersionUID = 4922467054075409408L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "program_id", nullable = false)
  private Program program;

  @Comment("그룹명")
  @Column(name = "name", nullable = false, length = 30)
  private String name;

  @Comment("순서")
  @Column(name = "orders", nullable = false)
  private Long order;

  @OneToMany(mappedBy = "programWriter", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("order ASC")
  private List<ProgramWriterInfo> infos = new ArrayList<>();

  public ProgramWriter(PostAdminProgramRequest.Writer dto, Program program, long order) {
    this.program = program;
    this.name = dto.getName();
    this.order = order;
    this.infos =
        IntStream.range(0, dto.getInfos().size())
            .mapToObj(index -> new ProgramWriterInfo(dto.getInfos().get(index), this, index))
            .collect(Collectors.toList());
  }

  public void update(PostAdminProgramRequest.Writer dto, long order) {
    this.name = dto.getName();
    this.order = order;
    this.getInfos()
        .removeIf(
            info ->
                dto.getInfos().stream()
                    .noneMatch(
                        dtoInfo ->
                            dtoInfo.getId() != null && dtoInfo.getId() == info.getId().intValue()));
    IntStream.range(0, dto.getInfos().size())
        .forEach(
            index -> {
              PostAdminProgramRequest.Writer.WriterInfo dtoInfo = dto.getInfos().get(index);
              ProgramWriterInfo up =
                  this.getInfos().stream()
                      .filter(
                          writer ->
                              dtoInfo.getId() != null
                                  && writer.getId().intValue() == dtoInfo.getId())
                      .findAny()
                      .orElse(null);
              if (up == null) {
                this.addInfo(new ProgramWriterInfo(dtoInfo, this, index));
              } else {
                up.update(dtoInfo, index);
              }
            });
  }

  private void addInfo(ProgramWriterInfo entity) {
    this.infos.add(entity);
  }
}
