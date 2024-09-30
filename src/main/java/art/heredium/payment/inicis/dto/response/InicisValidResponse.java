package art.heredium.payment.inicis.dto.response;

import art.heredium.domain.ticket.entity.Ticket;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InicisValidResponse {
    private String timestamp;
    private String oid;
    private Long price;
    private String mid;
    private String mkey;
    private String signature;
    private String goodName;
    private String buyerName;
    private String buyerTel;
    private String buyerEmail;

    public InicisValidResponse(Ticket ticket, String timestamp, String oid, Long price, String mid, String mkey, String signature) {
        this.goodName = ticket.getTitle();
        this.buyerName = ticket.getName();
        this.buyerTel = ticket.getPhone();
        this.buyerEmail = ticket.getEmail();
        this.timestamp = timestamp;
        this.oid = oid;
        this.price = price;
        this.mid = mid;
        this.mkey = mkey;
        this.signature = signature;
    }
}