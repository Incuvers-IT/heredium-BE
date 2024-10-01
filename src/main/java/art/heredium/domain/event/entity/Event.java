package art.heredium.domain.event.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.common.entity.BaseEntity;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.DateState;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.domain.event.model.dto.request.PostAdminEventRequest;
import art.heredium.domain.log.entity.Log;
import art.heredium.domain.log.type.LogAction;
import art.heredium.domain.log.type.LogType;
import art.heredium.ncloud.bean.CloudStorage;

@Entity
@Getter
@Table(name = "event")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
// 이벤트
public class Event extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 3879860526400989933L;

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

  @Comment("시작일")
  @Column(name = "start_date", nullable = false)
  private LocalDateTime startDate;

  @Comment("종료일")
  @Column(name = "end_date", nullable = false)
  private LocalDateTime endDate;

  @Comment("활성화 여부")
  @Column(name = "is_enabled", nullable = false)
  private Boolean isEnabled;

  @Comment("내용")
  @Column(name = "contents", nullable = false, columnDefinition = "TEXT")
  private String contents;

  @Comment("첨부파일")
  @Type(type = "json")
  @Column(name = "files", columnDefinition = "json", nullable = false)
  private List<Storage> files = new ArrayList<>();

  public Event(PostAdminEventRequest dto) {
    this.title = dto.getTitle();
    this.isEnabled = dto.getIsEnabled();
    this.startDate = dto.getStartDate();
    this.endDate = dto.getEndDate();
    this.contents = dto.getContents();
    this.files = dto.getFiles();
    this.thumbnail = dto.getThumbnail();
  }

  public void update(PostAdminEventRequest dto) {
    this.title = dto.getTitle();
    this.isEnabled = dto.getIsEnabled();
    this.startDate = dto.getStartDate();
    this.endDate = dto.getEndDate();
    this.contents = dto.getContents();
    this.files = dto.getFiles();
    this.thumbnail = dto.getThumbnail();
  }

  public List<String> getRemoveFile(PostAdminEventRequest dto) {
    List<Storage> entityStorages = new ArrayList<>();
    entityStorages.add(this.getThumbnail());
    entityStorages.addAll(this.getFiles());
    List<Storage> dtoStorages = new ArrayList<>();
    dtoStorages.add(dto.getThumbnail());
    dtoStorages.addAll(dto.getFiles());
    return entityStorages.stream()
        .filter(
            entityStorage ->
                entityStorage != null
                    && dtoStorages.stream()
                        .noneMatch(
                            dtoStorage ->
                                dtoStorage != null
                                    && dtoStorage
                                        .getSavedFileName()
                                        .equals(entityStorage.getSavedFileName())))
        .flatMap(x -> x.getAllFileName().stream())
        .collect(Collectors.toList());
  }

  public void applyTempContents(CloudStorage cloudStorage) {
    String path = getFileFolderPath();
    List<String> tempFiles = Constants.getEditorTempFile(this.contents, path);
    updateContent(Constants.replaceTempFile(this.contents, tempFiles, path));
    tempFiles.forEach(x -> cloudStorage.move(x, x.replace(FilePathType.EDITOR.getPath(), path)));
  }

  public void applyTempFile(CloudStorage cloudStorage) {
    this.files.forEach(file -> Constants.moveFileFromTemp(cloudStorage, file, getFileFolderPath()));
    Constants.moveFileFromTemp(cloudStorage, this.thumbnail, getFileFolderPath());
  }

  public void updateContent(String contents) {
    this.contents = contents;
  }

  public Log createInsertLog(Admin admin) {
    return new Log(admin, this.title, this.toString(), LogType.EVENT, LogAction.INSERT);
  }

  public Log createUpdateLog(Admin admin) {
    return new Log(admin, this.title, this.toString(), LogType.EVENT, LogAction.UPDATE);
  }

  public Log createDeleteLog(Admin admin) {
    return new Log(admin, this.title, this.toString(), LogType.EVENT, LogAction.DELETE);
  }

  public String getFileFolderPath() {
    return FilePathType.EVENT.getPath() + "/" + this.id;
  }

  public DateState getState() {
    return DateState.getState(this.getStartDate(), this.getEndDate());
  }
}
