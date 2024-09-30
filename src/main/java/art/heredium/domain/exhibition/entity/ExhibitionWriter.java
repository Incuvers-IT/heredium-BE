package art.heredium.domain.exhibition.entity;

import art.heredium.domain.exhibition.model.dto.request.PostAdminExhibitionRequest;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Entity
@Getter
@Table(name = "exhibition_writer")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"exhibition"})
//전시 인원 소개
public class ExhibitionWriter implements Serializable {
    private static final long serialVersionUID = 4922467054075409408L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exhibition_id", nullable = false)
    private Exhibition exhibition;

    @Comment("그룹명")
    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Comment("순서")
    @Column(name = "orders", nullable = false)
    private Long order;

    @OneToMany(mappedBy = "exhibitionWriter", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("order ASC")
    private List<ExhibitionWriterInfo> infos = new ArrayList<>();

    public ExhibitionWriter(PostAdminExhibitionRequest.Writer dto, Exhibition exhibition, long order) {
        this.exhibition = exhibition;
        this.name = dto.getName();
        this.order = order;
        this.infos = IntStream.range(0, dto.getInfos().size()).mapToObj(index -> new ExhibitionWriterInfo(dto.getInfos().get(index), this, index)).collect(Collectors.toList());
    }

    public void update(PostAdminExhibitionRequest.Writer dto, long order) {
        this.name = dto.getName();
        this.order = order;
        this.getInfos().removeIf(info -> dto.getInfos().stream().noneMatch(dtoInfo -> dtoInfo.getId() != null && dtoInfo.getId() == info.getId().intValue()));
        IntStream.range(0, dto.getInfos().size()).forEach(index -> {
            PostAdminExhibitionRequest.Writer.WriterInfo dtoInfo = dto.getInfos().get(index);
            ExhibitionWriterInfo up = this.getInfos().stream().filter(writer -> dtoInfo.getId() != null && writer.getId().intValue() == dtoInfo.getId()).findAny().orElse(null);
            if (up == null) {
                this.addInfo(new ExhibitionWriterInfo(dtoInfo, this, index));
            } else {
                up.update(dtoInfo, index);
            }
        });
    }

    private void addInfo(ExhibitionWriterInfo entity) {
        this.infos.add(entity);
    }
}