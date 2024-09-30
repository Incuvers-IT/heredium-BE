package art.heredium.domain.event.model.dto.response;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.event.entity.Event;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GetAdminEventDetailResponse {
    private Long id;
    private String title;
    private Boolean isEnabled;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String contents;
    private List<Storage> files;
    private Storage thumbnail;
    private String createdName;
    private LocalDateTime createdDate;
    private String lastModifiedName;
    private LocalDateTime lastModifiedDate;


    public GetAdminEventDetailResponse(Event entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.isEnabled = entity.getIsEnabled();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.contents = entity.getContents();
        this.files = entity.getFiles();
        this.thumbnail = entity.getThumbnail();
        this.createdName = entity.getCreatedName();
        this.createdDate = entity.getCreatedDate();
        this.lastModifiedName = entity.getLastModifiedName();
        this.lastModifiedDate = entity.getLastModifiedDate();
    }
}
