package art.heredium.domain.ticket.entity;

import art.heredium.domain.ticket.model.TicketOrderInfo;
import art.heredium.domain.ticket.model.TicketUserInfo;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "ticket_uuid")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
//티켓 결제 완료 전 확인용 정보
public class TicketUuid implements Serializable, Persistable<String> {
    private static final long serialVersionUID = -4216143291540230371L;

    @Id
    private String uuid;

    @Column(name = "account_id", updatable = false)
    private Long accountId;

    @Column(name = "non_user_id", updatable = false)
    private Long nonUserId;

    @Comment("티켓 구매자 정보")
    @Type(type = "json")
    @Column(name = "ticket_user_info", columnDefinition = "json", updatable = false)
    private TicketUserInfo ticketUserInfo;

    @Comment("티켓 정보")
    @Type(type = "json")
    @Column(name = "info", columnDefinition = "json", nullable = false, updatable = false)
    private TicketOrderInfo info;

    @Comment("생성일")
    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Override
    public String getId() {
        return this.uuid;
    }

    @Override
    public boolean isNew() {
        return this.getCreatedDate() == null;
    }

    public TicketUuid(String uuid, TicketUserInfo ticketUserInfo, TicketOrderInfo info) {
        this.uuid = uuid;
        this.accountId = ticketUserInfo.getAccountId();
        this.nonUserId = ticketUserInfo.getNonUserId();
        this.ticketUserInfo = ticketUserInfo;
        this.info = info;
    }
}
