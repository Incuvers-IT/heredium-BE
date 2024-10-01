package art.heredium.domain.ticket.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.domain.ticket.type.TicketType;

@Getter
@Setter
public class GetAdminTicketResponse {
  private Long id;
  private TicketKindType kind;
  private TicketType type;
  private String title;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private Integer number;
  private Long price;
  private String email;
  private String phone;
  private String name;
  private String uuid;
  private String pgId;
  private LocalDateTime createdDate;
  private TicketStateType state;

  public GetAdminTicketResponse(Ticket entity) {
    this.id = entity.getId();
    this.kind = entity.getKind();
    this.type = entity.getType();
    this.title = entity.getTitle();
    this.startDate = entity.getStartDate();
    this.endDate = entity.getEndDate();
    this.number = entity.getNumber();
    this.price = entity.getPrice();
    this.email = entity.getEmail();
    this.phone = entity.getPhone();
    this.name = entity.getName();
    this.uuid = entity.getUuid();
    this.pgId = entity.getPgId();
    this.createdDate = entity.getCreatedDate();
    this.state = entity.getState();
  }
}
