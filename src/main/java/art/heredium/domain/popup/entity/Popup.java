package art.heredium.domain.popup.entity;

import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.common.entity.BaseEntity;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.DateState;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.domain.log.entity.Log;
import art.heredium.domain.log.type.LogAction;
import art.heredium.domain.log.type.LogType;
import art.heredium.domain.popup.model.dto.request.PostAdminPopupRequest;
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
@Table(name = "popup")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
//팝업
public class Popup extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 754656425006161838L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("제목")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

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

    @Comment("오늘 숨기기 표기 여부")
    @Column(name = "is_hide_today", nullable = false)
    private Boolean isHideToday;

    @Comment("새창 열기 여부")
    @Column(name = "is_new_tab", nullable = false)
    private Boolean isNewTab;

    @Comment("링크")
    @Column(name = "link", nullable = false, length = 2048)
    private String link;

    @Comment("순서")
    @Column(name = "orders", nullable = false)
    private Long order;

    public Popup(PostAdminPopupRequest dto, Long order) {
        this.title = dto.getTitle();
        this.isEnabled = dto.getIsEnabled();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
        this.pcImage = dto.getPcImage();
        this.pcImageAlt = dto.getPcImageAlt();
        this.mobileImage = dto.getMobileImage();
        this.mobileImageAlt = dto.getMobileImageAlt();
        this.isHideToday = dto.getIsHideToday();
        this.isNewTab = dto.getIsNewTab();
        this.link = dto.getLink();
        this.order = order;
    }

    public void update(PostAdminPopupRequest dto) {
        this.title = dto.getTitle();
        this.isEnabled = dto.getIsEnabled();
        this.startDate = dto.getStartDate();
        this.endDate = dto.getEndDate();
        this.pcImage = dto.getPcImage();
        this.pcImageAlt = dto.getPcImageAlt();
        this.mobileImage = dto.getMobileImage();
        this.mobileImageAlt = dto.getMobileImageAlt();
        this.isHideToday = dto.getIsHideToday();
        this.isNewTab = dto.getIsNewTab();
        this.link = dto.getLink();
    }

    public List<String> getRemoveFile(PostAdminPopupRequest dto) {
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
        return new Log(admin, this.title, this.toString(), LogType.POPUP, LogAction.INSERT);
    }

    public Log createUpdateLog(Admin admin) {
        return new Log(admin, this.title, this.toString(), LogType.POPUP, LogAction.UPDATE);
    }

    public Log createDeleteLog(Admin admin) {
        return new Log(admin, this.title, this.toString(), LogType.POPUP, LogAction.DELETE);
    }

    public String getFileFolderPath() {
        return FilePathType.POPUP.getPath() + "/" + this.id;
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