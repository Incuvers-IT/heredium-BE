package art.heredium.domain.ticket.model.dto.request;

import art.heredium.domain.ticket.type.TicketKindType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PostAdminTicketGroupRequest {
    @NotNull
    private Long accountId;
    @NotNull
    private TicketKindType kind;
    @NotNull
    private Long roundId;
    @NotNull
    private Integer number;
    @NotNull
    private Long price;
}