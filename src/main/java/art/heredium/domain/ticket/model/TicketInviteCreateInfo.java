package art.heredium.domain.ticket.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.ticket.type.TicketKindType;

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
