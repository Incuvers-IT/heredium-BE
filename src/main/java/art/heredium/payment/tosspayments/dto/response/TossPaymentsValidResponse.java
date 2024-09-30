package art.heredium.payment.tosspayments.dto.response;

import art.heredium.domain.ticket.entity.Ticket;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TossPaymentsValidResponse {
    private String uuid;
    private String title;
    private Long amount;
    private String name;
    private String email;

    public TossPaymentsValidResponse(Ticket ticket) {
        this.uuid = ticket.getUuid();
        this.title = ticket.getTitle();
        this.amount = ticket.getPrice();
        this.name = ticket.getName();
        this.email = ticket.getEmail();
    }
}