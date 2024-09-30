package art.heredium.domain.ticket.entity;

import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.ticket.type.TicketStateType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "ticket_log")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"ticket"})
//티켓 로그
public class TicketLog implements Serializable {
    private static final long serialVersionUID = 1144349900229680727L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    private Ticket ticket;

    @Comment("수정 관리자")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @Comment("수정자 이름")
    @Column(name = "name", nullable = false, length = 30, updatable = false)
    private String name;

    @Comment("이전상태")
    @Column(name = "prestate", updatable = false)
    @Convert(converter = TicketStateType.Converter.class)
    private TicketStateType preState;

    @Comment("수정상태")
    @Column(name = "state", nullable = false, updatable = false)
    @Convert(converter = TicketStateType.Converter.class)
    private TicketStateType state;

    @Comment("생성일")
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    public TicketLog(Ticket ticket, Admin admin, String name, TicketStateType preState, TicketStateType state) {
        this.ticket = ticket;
        this.admin = admin;
        this.name = name;
        this.preState = preState;
        this.state = state;
    }
}
