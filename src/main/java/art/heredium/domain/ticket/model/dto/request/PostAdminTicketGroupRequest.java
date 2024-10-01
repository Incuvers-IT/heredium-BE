package art.heredium.domain.ticket.model.dto.request;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.ticket.type.TicketKindType;

@Getter
@Setter
public class PostAdminTicketGroupRequest {
  @NotNull private Long accountId;
  @NotNull private TicketKindType kind;
  @NotNull private Long roundId;
  @NotNull private Integer number;
  @NotNull private Long price;
}
