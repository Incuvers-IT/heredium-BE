package art.heredium.domain.ticket.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.entity.TicketPrice;
import art.heredium.domain.ticket.type.TicketKindType;

@Getter
@Setter
public class PostUserTicketResponse {
  private String uuid;
  private String title;
  private TicketKindType kind;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private Long price;
  private List<Price> prices;

  @Getter
  @Setter
  private static class Price {
    private String type;
    private Integer number;

    private Price(TicketPrice entity) {
      this.type = entity.getType();
      this.number = entity.getNumber();
    }
  }

  public PostUserTicketResponse(Ticket entity) {
    this.uuid = entity.getUuid();
    this.title = entity.getTitle();
    this.kind = entity.getKind();
    this.startDate = entity.getStartDate();
    this.endDate = entity.getEndDate();
    this.price = entity.getPrice();
    this.prices = entity.getPrices().stream().map(Price::new).collect(Collectors.toList());
  }
}
