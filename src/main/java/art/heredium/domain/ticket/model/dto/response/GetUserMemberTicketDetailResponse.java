package art.heredium.domain.ticket.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.entity.TicketPrice;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.domain.ticket.type.TicketType;

@Getter
@Setter
public class GetUserMemberTicketDetailResponse {
  private Long id;
  private Storage thumbnail;
  private TicketKindType kind;
  private TicketType type;
  private String title;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private Integer number;
  private Long price;
  private Long originPrice;
  private String payMethod;
  private String uuid;
  private LocalDateTime createdDate;
  private TicketStateType state;
  private List<Price> prices;

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

  public GetUserMemberTicketDetailResponse(Ticket entity, Storage thumbnail) {
    this.id = entity.getId();
    this.thumbnail = thumbnail;
    this.kind = entity.getKind();
    this.type = entity.getType();
    this.title = entity.getTitle();
    this.startDate = entity.getStartDate();
    this.endDate = entity.getEndDate();
    this.number = entity.getNumber();
    this.price = entity.getPrice();
    this.originPrice = entity.getOriginPrice();
    this.payMethod = entity.getPayMethod();
    this.uuid = entity.getUuid();
    this.createdDate = entity.getCreatedDate();
    this.state = entity.getState();
    this.prices = entity.getPrices().stream().map(Price::new).collect(Collectors.toList());
  }
}
