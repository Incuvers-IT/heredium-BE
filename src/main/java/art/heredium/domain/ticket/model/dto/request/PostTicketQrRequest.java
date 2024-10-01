package art.heredium.domain.ticket.model.dto.request;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.ticket.type.TicketKindType;

@Getter
@Setter
public class PostTicketQrRequest {
  @NotNull private Long id;
  @NotNull private String uuid;
  @NotNull private List<@Valid Allow> allows;

  @Getter
  @Setter
  public static class Allow {
    @NotNull private Long id;
    @NotNull private TicketKindType kind;
  }
}
