package art.heredium.domain.ticket.model.dto.request;

import art.heredium.domain.ticket.type.TicketKindType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class PostTicketQrRequest {
    @NotNull
    private Long id;
    @NotNull
    private String uuid;
    @NotNull
    private List<@Valid Allow> allows;

    @Getter
    @Setter
    public static class Allow {
        @NotNull
        private Long id;
        @NotNull
        private TicketKindType kind;
    }
}