package art.heredium.domain.account.model.dto.response;

import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.domain.ticket.type.TicketType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetAdminAccountTicketResponse {
    private Long id;
    private TicketType type;
    private TicketKindType kind;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer number;
    private Long price;
    private String email;
    private String name;
    private String phone;
    private String uuid;
    private String pgId;
    private LocalDateTime createdDate;
    private TicketStateType state;

    public GetAdminAccountTicketResponse(Ticket entity) {
        this.id = entity.getId();
        this.type = entity.getType();
        this.kind = entity.getKind();
        this.title = entity.getTitle();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.number = entity.getNumber();
        this.price = entity.getPrice();
        this.email = entity.getEmail();
        this.name = entity.getName();
        this.phone = entity.getPhone();
        this.uuid = entity.getUuid();
        this.pgId = entity.getPgId();
        this.createdDate = entity.getCreatedDate();
        this.state = entity.getState();
    }
}