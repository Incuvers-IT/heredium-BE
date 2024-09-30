package art.heredium.domain.ticket.model;

import art.heredium.domain.ticket.type.TicketKindType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TicketInviteCreateInfo {
    private TicketKindType kind;
    private Long id;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer number;
}