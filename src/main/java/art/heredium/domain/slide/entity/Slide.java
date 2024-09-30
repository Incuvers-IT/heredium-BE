package art.heredium.domain.slide.entity;

import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.common.entity.BaseEntity;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.DateState;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.domain.log.entity.Log;
import art.heredium.domain.log.type.LogAction;
import art.heredium.domain.log.type.LogType;
import art.heredium.domain.slide.model.dto.request.PostAdminSlideRequest;
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

@Entity
@Getter
@Table(name = "slide")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
//슬라이더
public class Slide extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -132582139277799191L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("제목")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Comment("부제목")
    @Column(name = "subtitle", nullable = false, length = 100)
    private String subtitle;

    @Comment("스케줄")
    @Column(name = "schedule", nullable = false, length = 100)
    private String schedule;

    @Comment("활성화 여부")
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @Comment("시작일")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Comment("종료일")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Comment("pc 이미지")
    @Type(type = "json")
    @Column(name = "pc_image", columnDefinition = "json", nullable = false)
    private Storage pcImage;

    @Comment("pc 이미지 대체 텍스트")
    @Column(name = "pc_image_alt", nullable = false, length = 100)
    private String pcImageAlt;

    @Comment("모바일 이미지")
    @Type(type = "json")
    @Column(name = "mobile_image", columnDefinition = "json", nullable = false)
    private Storage mobileImage;

    @Comment("모바일 이미지 대체 텍스트")
    @Column(name = "mobile_image_alt", nullable = false, length = 100)
    private String mobileImageAlt;

    @Comment("버튼 표기 여부")
    @Column(name = "is_use_button", nullable = false)
    private Boolean isUseButton;

    @Comment("새창 열기 여부")
    @Column(name = "is_new_tab", nullable = false)
    private Boolean isNewTab;

    @Comment("버튼 링크")
    @Column(name = "link", nullable = false, length = 2048)
    private String link;

    @Comment("순서")
    @Column(name = "orders", nullable = false)
    private Long order;

    public Slide(PostAdminSlideRequest dto, Long order) {
        this.title = dto.getTitle();
        this.subtitle = dto.getSubtitle();
        this.schedule = dto.getSchedule();
        this.isEnabled = dto.getIsEnabled();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
        this.pcImage = dto.getPcImage();
        this.pcImageAlt = dto.getPcImageAlt();
        this.mobileImage = dto.getMobileImage();
        this.mobileImageAlt = dto.getMobileImageAlt();
        this.isUseButton = dto.getIsUseButton();
        this.isNewTab = dto.getIsNewTab();
        this.link = dto.getLink();
        this.order = order;
    }

    public void update(PostAdminSlideRequest dto) {
        this.title = dto.getTitle();
        this.subtitle = dto.getSubtitle();
        this.schedule = dto.getSchedule();
        this.isEnabled = dto.getIsEnabled();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
        this.pcImage = dto.getPcImage();
        this.pcImageAlt = dto.getPcImageAlt();
        this.mobileImage = dto.getMobileImage();
        this.mobileImageAlt = dto.getMobileImageAlt();
        this.isUseButton = dto.getIsUseButton();
        this.isNewTab = dto.getIsNewTab();
        this.link = dto.getLink();
    }

    public List<String> getRemoveFile(PostAdminSlideRequest dto) {
        List<Storage> entityStorages = new ArrayList<>();
        entityStorages.add(this.getPcImage());
        entityStorages.add(this.getMobileImage());
        List<Storage> dtoStorages = new ArrayList<>();
        dtoStorages.add(dto.getPcImage());
        dtoStorages.add(dto.getMobileImage());
        return entityStorages.stream()
                .filter(entityStorage -> entityStorage != null
                        && dtoStorages.stream()
                        .noneMatch(dtoStorage -> dtoStorage != null && dtoStorage.getSavedFileName().equals(entityStorage.getSavedFileName()))
                )
                .flatMap(x->x.getAllFileName().stream()).collect(Collectors.toList());
    }

    public void applyTempFile(CloudStorage cloudStorage) {
        Constants.moveFileFromTemp(cloudStorage, this.getPcImage(), getFileFolderPath());
        Constants.moveFileFromTemp(cloudStorage, this.getMobileImage(), getFileFolderPath());
    }

    public Log createInsertLog(Admin admin) {
        return new Log(admin, this.title, this.toString(), LogType.SLIDE, LogAction.INSERT);
    }

    public Log createUpdateLog(Admin admin) {
        return new Log(admin, this.title, this.toString(), LogType.SLIDE, LogAction.UPDATE);
    }

    public Log createDeleteLog(Admin admin) {
        return new Log(admin, this.title, this.toString(), LogType.SLIDE, LogAction.DELETE);
    }

    public String getFileFolderPath() {
        return FilePathType.SLIDE.getPath() + "/" + this.id;
    }

    public void updateOrder(Long order) {
        this.order = order;
    }

    public void updateEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public DateState getState() {
        return DateState.getState(this.getStartDate(), this.getEndDate());
    }
}