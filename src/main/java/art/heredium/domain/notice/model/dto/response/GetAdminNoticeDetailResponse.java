package art.heredium.domain.notice.model.dto.response;

import art.heredium.domain.notice.entity.Notice;
import art.heredium.domain.common.model.Storage;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GetAdminNoticeDetailResponse {
    private Long id;
    private String title;
    private Boolean isEnabled;
    private Boolean isNotice;
    private LocalDateTime postDate;
    private String contents;
    private List<Storage> files;
    private String createdName;
    private LocalDateTime createdDate;
    private String lastModifiedName;
    private LocalDateTime lastModifiedDate;


    public GetAdminNoticeDetailResponse(Notice entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.isEnabled = entity.getIsEnabled();
        this.isNotice = entity.getIsNotice();
        this.postDate = entity.getPostDate();
        this.contents = entity.getContents();
        this.files = entity.getFiles();
        this.createdName = entity.getCreatedName();
        this.createdDate = entity.getCreatedDate();
        this.lastModifiedName = entity.getLastModifiedName();
        this.lastModifiedDate = entity.getLastModifiedDate();
    }
}
