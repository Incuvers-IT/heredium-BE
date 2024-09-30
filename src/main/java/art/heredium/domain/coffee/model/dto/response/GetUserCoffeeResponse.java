package art.heredium.domain.coffee.model.dto.response;

import art.heredium.domain.coffee.entity.Coffee;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.ProjectStateType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetUserCoffeeResponse {
    private Long id;
    private Storage thumbnail;
    private ProjectStateType state;
    private String title;
    private String subtitle;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public GetUserCoffeeResponse(Coffee entity) {
        this.id = entity.getId();
        this.thumbnail = entity.getThumbnail();
        this.state = entity.getState();
        this.title = entity.getTitle();
        this.subtitle = entity.getSubtitle();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
    }
}
