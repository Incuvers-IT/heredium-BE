package art.heredium.domain.docent.entity;

import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.common.entity.BaseEntity;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.DateState;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.domain.common.type.HallType;
import art.heredium.domain.docent.model.dto.request.PostAdminDocentRequest;
import art.heredium.domain.log.entity.Log;
import art.heredium.domain.log.type.LogAction;
import art.heredium.domain.log.type.LogType;
import art.heredium.ncloud.bean.CloudStorage;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Entity
@Getter
@Table(name = "docent")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
//도슨트
public class Docent extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -5125121271289308687L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("썸네일")
    @Type(type = "json")
    @Column(name = "thumbnail", columnDefinition = "json")
    private Storage thumbnail;

    @Comment("제목")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Comment("부제목")
    @Column(name = "subtitle", nullable = false, length = 100)
    private String subtitle;

    @Comment("hall 구분")
    @Type(type = "json")
    @Column(columnDefinition = "json", nullable = false)
    private List<HallType> halls = new ArrayList<>();

    @Comment("활성화 여부")
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @Comment("시작일")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Comment("종료일")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "docent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("order ASC")
    private List<DocentInfo> infos = new ArrayList<>();

    public Docent(PostAdminDocentRequest dto) {
        this.thumbnail = dto.getThumbnail();
        this.title = dto.getTitle();
        this.subtitle = dto.getSubtitle();
        this.halls = dto.getHalls();
        this.isEnabled = dto.getIsEnabled();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
        this.infos = IntStream.range(0, dto.getInfos().size()).mapToObj(index -> new DocentInfo(dto.getInfos().get(index), this, index)).collect(Collectors.toList());
    }

    public void update(PostAdminDocentRequest dto) {
        this.thumbnail = dto.getThumbnail();
        this.title = dto.getTitle();
        this.subtitle = dto.getSubtitle();
        this.halls = dto.getHalls();
        this.isEnabled = dto.getIsEnabled();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();

        this.getInfos().removeIf(writer -> dto.getInfos().stream().noneMatch(dtoWriter -> dtoWriter.getId() != null && dtoWriter.getId() == writer.getId().intValue()));
        IntStream.range(0, dto.getInfos().size()).forEach(index -> {
            PostAdminDocentRequest.Info dtoInfo = dto.getInfos().get(index);
            DocentInfo up = this.getInfos().stream().filter(writer -> dtoInfo.getId() != null && writer.getId().intValue() == dtoInfo.getId()).findAny().orElse(null);
            if (up == null) {
                this.addInfo(new DocentInfo(dtoInfo, this, index));
            } else {
                up.update(dtoInfo, index);
            }
        });
    }

    public void addInfo(DocentInfo entity) {
        this.infos.add(entity);
    }

    public List<String> getRemoveFile(PostAdminDocentRequest dto) {
        List<Storage> entityStorages = new ArrayList<>();
        entityStorages.add(this.getThumbnail());
        this.getInfos().forEach(di -> {
            entityStorages.add(di.getThumbnail());
            entityStorages.add(di.getAudio());
            entityStorages.add(di.getMap());
        });
        List<Storage> dtoStorages = new ArrayList<>();
        dtoStorages.add(dto.getThumbnail());
        dto.getInfos().forEach(di -> {
            dtoStorages.add(di.getThumbnail());
            dtoStorages.add(di.getAudio());
            dtoStorages.add(di.getMap());
        });

        return entityStorages.stream()
                .filter(entityStorage -> entityStorage != null
                        && dtoStorages.stream()
                        .noneMatch(dtoStorage -> dtoStorage != null && dtoStorage.getSavedFileName().equals(entityStorage.getSavedFileName()))
                )
                .flatMap(x->x.getAllFileName().stream()).collect(Collectors.toList());
    }

    public void applyTempFile(CloudStorage cloudStorage) {
        Constants.moveFileFromTemp(cloudStorage, this.getThumbnail(), getFileFolderPath());
        this.getInfos().forEach(info -> {
            Constants.moveFileFromTemp(cloudStorage, info.getThumbnail(), getFileFolderPath());
            Constants.moveFileFromTemp(cloudStorage, info.getAudio(), getFileFolderPath());
            Constants.moveFileFromTemp(cloudStorage, info.getMap(), getFileFolderPath());
        });
    }

    public Log createInsertLog(Admin admin) {
        return new Log(admin, this.title, this.toString(), LogType.DOCENT, LogAction.INSERT);
    }

    public Log createUpdateLog(Admin admin) {
        return new Log(admin, this.title, this.toString(), LogType.DOCENT, LogAction.UPDATE);
    }

    public Log createDeleteLog(Admin admin) {
        return new Log(admin, this.title, this.toString(), LogType.DOCENT, LogAction.DELETE);
    }

    public String getFileFolderPath() {
        return FilePathType.DOCENT.getPath() + "/" + this.id;
    }

    public DateState getState() {
        return DateState.getState(this.getStartDate(), this.getEndDate());
    }
}
