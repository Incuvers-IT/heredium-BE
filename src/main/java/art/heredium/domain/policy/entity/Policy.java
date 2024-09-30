package art.heredium.domain.policy.entity;

import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.common.entity.BaseEntity;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.domain.log.entity.Log;
import art.heredium.domain.log.type.LogAction;
import art.heredium.domain.log.type.LogType;
import art.heredium.domain.policy.model.dto.request.PostAdminPolicyRequest;
import art.heredium.domain.policy.type.PolicyType;
import art.heredium.ncloud.bean.CloudStorage;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Getter
@Table(name = "policy")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
//약관
public class Policy extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 5456418141542774272L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("제목")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Comment("활성화 여부")
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @Comment("게시일")
    @Column(name = "post_date", nullable = false)
    private LocalDateTime postDate;

    @Comment("내용")
    @Column(name = "contents", nullable = false, columnDefinition = "LONGTEXT")
    private String contents;

    @Comment("약관 종류")
    @Convert(converter = PolicyType.Converter.class)
    @Column(name = "type", nullable = false, updatable = false)
    private PolicyType type;

    public Policy(PostAdminPolicyRequest dto) {
        this.title = dto.getTitle();
        this.isEnabled = dto.getIsEnabled();
        this.postDate = dto.getPostDate();
        this.contents = dto.getContents();
        this.type = dto.getType();
    }

    public void update(PostAdminPolicyRequest dto) {
        this.title = dto.getTitle();
        this.isEnabled = dto.getIsEnabled();
        this.postDate = dto.getPostDate();
        this.contents = dto.getContents();
    }

    public void applyTempContents(CloudStorage cloudStorage) {
        String path = getFileFolderPath();
        List<String> tempFiles = Constants.getEditorTempFile(this.contents, path);
        updateContent(Constants.replaceTempFile(this.contents, tempFiles, path));
        tempFiles.forEach(x -> cloudStorage.move(x, x.replace(FilePathType.EDITOR.getPath(), path)));
    }

    public List<String> getRemoveFile(PostAdminPolicyRequest dto) {
        List<String> removeFiles = Constants.getRemoveImage(this.contents, dto.getContents());
        return removeFiles.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void updateContent(String contents) {
        this.contents = contents;
    }

    public Log createInsertLog(Admin admin) {
        return new Log(admin, this.title, this.toString(), LogType.POLICY, LogAction.INSERT);
    }

    public Log createUpdateLog(Admin admin) {
        return new Log(admin, this.title, this.toString(), LogType.POLICY, LogAction.UPDATE);
    }

    public Log createDeleteLog(Admin admin) {
        return new Log(admin, this.title, this.toString(), LogType.POLICY, LogAction.DELETE);
    }

    public String getFileFolderPath() {
        return FilePathType.POLICY.getPath() + "/" + this.id;
    }
}