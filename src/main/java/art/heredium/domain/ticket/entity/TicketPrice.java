package art.heredium.domain.ticket.entity;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Table(name = "ticket_price")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"ticket"})
//티켓 가격 정보
public class TicketPrice implements Serializable {
    private static final long serialVersionUID = 3489094738347577328L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    private Ticket ticket;

    @Comment("종류")
    @Column(name = "type", nullable = false, updatable = false)
    private String type;

    @Comment("구매수")
    @Column(name = "number", nullable = false, updatable = false)
    private Integer number;

    @Comment("최종 결제 가격")
    @Column(name = "price", nullable = false, updatable = false)
    private Long price;

    @Comment("가격(할인 전 가격)")
    @Column(name = "origin_price", nullable = false, updatable = false)
    private Long originPrice;

    @Comment("비고")
    @Column(name = "note", updatable = false)
    private String note;

    public TicketPrice(Ticket ticket, String type, Integer number, Long price, Long originPrice, String note) {
        this.ticket = ticket;
        this.type = type;
        this.number = number;
        this.price = price;
        this.originPrice = originPrice;
        this.note = note;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
}
