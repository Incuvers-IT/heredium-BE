package art.heredium.domain.notice.entity;

import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.common.entity.BaseEntity;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.domain.log.entity.Log;
import art.heredium.domain.log.type.LogAction;
import art.heredium.domain.log.type.LogType;
import art.heredium.domain.notice.model.dto.request.PostAdminNoticeRequest;
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
@Table(name = "notice")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
//공지사항
public class Notice extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 556285114857979942L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("제목")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Comment("활성화 여부")
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @Comment("상단고정 여부")
    @Column(name = "is_notice", nullable = false)
    private Boolean isNotice;

    @Comment("게시일")
    @Column(name = "post_date", nullable = false)
    private LocalDateTime postDate;

    @Comment("내용")
    @Column(name = "contents", nullable = false, columnDefinition = "TEXT")
    private String contents;

    @Comment("첨부파일")
    @Type(type = "json")
    @Column(name = "files", columnDefinition = "json", nullable = false)
    private List<Storage> files = new ArrayList<>();

    public Notice(PostAdminNoticeRequest dto) {
        this.title = dto.getTitle();
        this.isEnabled = dto.getIsEnabled();
        this.isNotice = dto.getIsNotice();
        this.postDate = dto.getPostDate();
        this.contents = dto.getContents();
        this.files = dto.getFiles();
    }

    public void update(PostAdminNoticeRequest dto) {
        this.title = dto.getTitle();
        this.isEnabled = dto.getIsEnabled();
        this.isNotice = dto.getIsNotice();
        this.postDate = dto.getPostDate();
        this.contents = dto.getContents();
        this.files = dto.getFiles();
    }

    public List<String> getRemoveFile(PostAdminNoticeRequest dto) {
        List<Storage> entityStorages = new ArrayList<>(this.getFiles());
        List<Storage> dtoStorages = new ArrayList<>(dto.getFiles());
        return entityStorages.stream()
                .filter(entityStorage -> entityStorage != null
                        && dtoStorages.stream()
                        .noneMatch(dtoStorage -> dtoStorage != null && dtoStorage.getSavedFileName().equals(entityStorage.getSavedFileName()))
                )
                .flatMap(x->x.getAllFileName().stream()).collect(Collectors.toList());
    }

    public void applyTempContents(CloudStorage cloudStorage) {
        String path = getFileFolderPath();
        List<String> tempFiles = Constants.getEditorTempFile(this.contents, path);
        updateContent(Constants.replaceTempFile(this.contents, tempFiles, path));
        tempFiles.forEach(x -> cloudStorage.move(x, x.replace(FilePathType.EDITOR.getPath(), path)));
    }

    public void applyTempFile(CloudStorage cloudStorage) {
        this.files.forEach(file -> Constants.moveFileFromTemp(cloudStorage, file, getFileFolderPath()));
    }

    public void updateContent(String contents) {
        this.contents = contents;
    }

    public Log createInsertLog(Admin admin) {
        return new Log(admin, this.title, this.toString(), LogType.NOTICE, LogAction.INSERT);
    }

    public Log createUpdateLog(Admin admin) {
        return new Log(admin, this.title, this.toString(), LogType.NOTICE, LogAction.UPDATE);
    }

    public Log createDeleteLog(Admin admin) {
        return new Log(admin, this.title, this.toString(), LogType.NOTICE, LogAction.DELETE);
    }

    public String getFileFolderPath() {
        return FilePathType.NOTICE.getPath() + "/" + this.id;
    }
}