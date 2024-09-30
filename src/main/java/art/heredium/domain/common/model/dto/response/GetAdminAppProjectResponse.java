package art.heredium.domain.common.model.dto.response;

import art.heredium.domain.coffee.entity.Coffee;
import art.heredium.domain.exhibition.entity.Exhibition;
import art.heredium.domain.program.entity.Program;
import art.heredium.domain.ticket.type.TicketKindType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetAdminAppProjectResponse {
    private Long id;
    private TicketKindType kind;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public GetAdminAppProjectResponse(Exhibition entity) {
        this.id = entity.getId();
        this.kind = TicketKindType.EXHIBITION;
        this.title = entity.getTitle();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
    }

    public GetAdminAppProjectResponse(Program entity) {
        this.id = entity.getId();
        this.kind = TicketKindType.PROGRAM;
        this.title = entity.getTitle();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
    }

    public GetAdminAppProjectResponse(Coffee entity) {
        this.id = entity.getId();
        this.kind = TicketKindType.COFFEE;
        this.title = entity.getTitle();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
    }
}
