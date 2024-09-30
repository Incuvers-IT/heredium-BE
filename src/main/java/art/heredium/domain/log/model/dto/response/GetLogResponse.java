package art.heredium.domain.log.model.dto.response;

import art.heredium.domain.log.entity.Log;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetLogResponse {
    private String name;
    private String email;
    private String type;
    private String action;
    private String title;
    private LocalDateTime createdDate;

    public GetLogResponse(Log entity) {
        this.name = entity.getName();
        this.email = entity.getEmail();
        this.type = entity.getType().getDesc();
        this.action = entity.getAction().getDesc();
        this.title = entity.getTitle();
        this.createdDate = entity.getCreatedDate();
    }
}
