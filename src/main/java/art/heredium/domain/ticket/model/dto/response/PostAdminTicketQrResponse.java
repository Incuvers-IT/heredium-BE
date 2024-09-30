package art.heredium.domain.ticket.model.dto.response;

import art.heredium.core.util.Constants;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostAdminTicketQrResponse {
    private Long id;
    private TicketKindType kind;
    private TicketType type;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime usedDate;
    private Integer number;
    private Long price;
    private String email;
    private String name;
    private String uuid;
    private String message;

    public PostAdminTicketQrResponse(Ticket entity) {
        this.id = entity.getId();
        this.kind = entity.getKind();
        this.type = entity.getType();
        this.title = entity.getTitle();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.usedDate = entity.getUsedDate();
        this.number = entity.getNumber();
        this.price = entity.getPrice();
        this.email = Constants.emailMasking(entity.getEmail());
        this.name = entity.getName().replaceAll(".(?=.$)", "*");
        this.uuid = entity.getUuid();
        this.message = String.format("입장권(%d명)이 사용완료로 변경되었습니다.", this.getNumber());
    }
}
