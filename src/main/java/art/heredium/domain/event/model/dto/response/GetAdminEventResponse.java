package art.heredium.domain.event.model.dto.response;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.DateState;
import art.heredium.domain.event.entity.Event;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetAdminEventResponse {
    private Long id;
    private Storage thumbnail;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String createdName;
    private LocalDateTime createdDate;
    private String lastModifiedName;
    private LocalDateTime lastModifiedDate;
    private Boolean isEnabled;
    private DateState state;

    public GetAdminEventResponse(Event entity) {
        this.id = entity.getId();
        this.thumbnail = entity.getThumbnail();
        this.title = entity.getTitle();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.createdName = entity.getCreatedName();
        this.createdDate = entity.getCreatedDate();
        this.lastModifiedName = entity.getLastModifiedName();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.isEnabled = entity.getIsEnabled();
        this.state = entity.getState();
    }
}
