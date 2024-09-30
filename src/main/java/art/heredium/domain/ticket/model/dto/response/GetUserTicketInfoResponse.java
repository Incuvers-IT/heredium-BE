package art.heredium.domain.ticket.model.dto.response;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.entity.TicketPrice;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.domain.ticket.type.TicketType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class GetUserTicketInfoResponse {
    private Long id;
    private TicketKindType kind;
    private TicketType type;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer number;
    private Long price;
    private Long originPrice;
    private String uuid;
    private TicketStateType state;
    private List<Price> prices;
    private Storage thumbnail;

    @Getter
    @Setter
    private static class Price {
        private String type;
        private Integer number;
        private Long price;
        private Long originPrice;

        private Price(TicketPrice entity) {
            this.type = entity.getType();
            this.number = entity.getNumber();
            this.price = entity.getPrice();
            this.originPrice = entity.getOriginPrice();
        }
    }

    public GetUserTicketInfoResponse(Ticket entity, Storage thumbnail) {
        this.id = entity.getId();
        this.kind = entity.getKind();
        this.type = entity.getType();
        this.title = entity.getTitle();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.number = entity.getNumber();
        this.price = entity.getPrice();
        this.originPrice = entity.getOriginPrice();
        this.uuid = entity.getUuid();
        this.state = entity.getState();
        this.prices = entity.getPrices().stream().map(Price::new).collect(Collectors.toList());
        this.thumbnail = thumbnail;
    }
}
