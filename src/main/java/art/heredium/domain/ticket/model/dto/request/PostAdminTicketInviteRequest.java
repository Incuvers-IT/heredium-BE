package art.heredium.domain.ticket.model.dto.request;

import art.heredium.domain.ticket.type.TicketKindType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
public class PostAdminTicketInviteRequest {
    @NotNull
    @Size(min = 1, max = 1000)
    private List<Long> accountIds;
    @NotNull
    private TicketKindType kind;
    @NotNull
    private Long id;
    @NotNull
    private Integer number;
}