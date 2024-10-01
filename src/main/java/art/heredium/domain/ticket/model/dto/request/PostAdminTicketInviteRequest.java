package art.heredium.domain.ticket.model.dto.request;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.ticket.type.TicketKindType;

@Getter
@Setter
public class PostAdminTicketInviteRequest {
  @NotNull
  @Size(min = 1, max = 1000)
  private List<Long> accountIds;

  @NotNull private TicketKindType kind;
  @NotNull private Long id;
  @NotNull private Integer number;
}
