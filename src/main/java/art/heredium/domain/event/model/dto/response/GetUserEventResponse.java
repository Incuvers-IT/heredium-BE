package art.heredium.domain.event.model.dto.response;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.DateState;
import art.heredium.domain.event.entity.Event;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetUserEventResponse {
    private Long id;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Storage thumbnail;
    private DateState state;

    public GetUserEventResponse(Event entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.thumbnail = entity.getThumbnail();
        this.state = entity.getState();
    }
}
