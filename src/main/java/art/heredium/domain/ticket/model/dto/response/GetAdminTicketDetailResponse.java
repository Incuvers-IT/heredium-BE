package art.heredium.domain.ticket.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.entity.TicketLog;
import art.heredium.domain.ticket.entity.TicketPrice;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.domain.ticket.type.TicketType;

@Getter
@Setter
public class GetAdminTicketDetailResponse {
  private Long id;
  private TicketKindType kind;
  private TicketType type;
  private String title;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private Integer number;
  private Long price;
  private Long originPrice;
  private String email;
  private String name;
  private String payMethod;
  private String phone;
  private String uuid;
  private String pgId;
  private LocalDateTime createdDate;
  private TicketStateType state;
  private List<Price> prices;
  private List<Log> logs;

  @Getter
  @Setter
  private static class Price {
    private String type;
    private Integer number;
    private Long price;
    private Long originPrice;
    private String note;

    private Price(TicketPrice entity) {
      this.type = entity.getType();
      this.number = entity.getNumber();
      this.price = entity.getPrice();
      this.originPrice = entity.getOriginPrice();
      this.note = entity.getNote();
    }
  }

  @Getter
  @Setter
  private static class Log {
    private String name;
    private TicketStateType preState;
    private TicketStateType state;
    private LocalDateTime createdDate;

    private Log(TicketLog entity) {
      this.name = entity.getName();
      this.preState = entity.getPreState();
      this.state = entity.getState();
      this.createdDate = entity.getCreatedDate();
    }
  }

  public GetAdminTicketDetailResponse(Ticket entity) {
    this.id = entity.getId();
    this.kind = entity.getKind();
    this.type = entity.getType();
    this.title = entity.getTitle();
    this.startDate = entity.getStartDate();
    this.endDate = entity.getEndDate();
    this.number = entity.getNumber();
    this.price = entity.getPrice();
    this.originPrice = entity.getOriginPrice();
    this.email = entity.getEmail();
    this.name = entity.getName();
    this.payMethod = entity.getPayMethod();
    this.phone = entity.getPhone();
    this.uuid = entity.getUuid();
    this.pgId = entity.getPgId();
    this.createdDate = entity.getCreatedDate();
    this.state = entity.getState();
    this.prices = entity.getPrices().stream().map(Price::new).collect(Collectors.toList());
    this.logs = entity.getLogs().stream().map(Log::new).collect(Collectors.toList());
  }
}
