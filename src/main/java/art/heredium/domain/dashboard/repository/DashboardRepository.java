package art.heredium.domain.dashboard.repository;

import art.heredium.domain.account.entity.QAccount;
import art.heredium.domain.ticket.entity.QTicket;
import art.heredium.domain.dashboard.model.dto.response.AdminDashBoardResponse;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.domain.ticket.type.TicketType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class DashboardRepository {

    private final JPAQueryFactory queryFactory;

    public AdminDashBoardResponse.Count dashboard(LocalDate now) {
        Integer visitCount = queryFactory
                .select(QTicket.ticket.number.sum().coalesce(0))
                .from(QTicket.ticket)
                .where(ticketUseDateEq(now), ticketKindNe(TicketKindType.COFFEE), ticketStateEq(TicketStateType.USED))
                .fetchOne();

        Integer totalCount = queryFactory
                .select(QTicket.ticket.number.sum().coalesce(0))
                .from(QTicket.ticket)
                .where(ticketStartDateEq(now), ticketKindNe(TicketKindType.COFFEE), ticketStateNe(TicketStateType.USER_REFUND), ticketStateNe(TicketStateType.ADMIN_REFUND), ticketTypeNe(TicketType.INVITE))
                .fetchOne();

        Integer saleCount = queryFactory
                .select(QTicket.ticket.number.sum().coalesce(0))
                .from(QTicket.ticket)
                .where(ticketCreateDateEq(now), ticketKindNe(TicketKindType.COFFEE), saleFilter())
                .fetchOne();

        Long salePrice = queryFactory
                .select(QTicket.ticket.price.sum().coalesce(0L))
                .from(QTicket.ticket)
                .where(ticketCreateDateEq(now), ticketKindNe(TicketKindType.COFFEE), saleFilter())
                .fetchOne();

        Long refundCount = queryFactory
                .select(Wildcard.count)
                .from(QTicket.ticket)
                .where(ticketCreateDateEq(now), refundFilter())
                .fetchOne();

        Long refundPrice = queryFactory
                .select(QTicket.ticket.price.sum().coalesce(0L))
                .from(QTicket.ticket)
                .where(ticketCreateDateEq(now), refundFilter())
                .fetchOne();

        Long newRegister = queryFactory
                .select(Wildcard.count)
                .from(QAccount.account)
                .where(accountCreatedDateEq(now))
                .fetchOne();

        return new AdminDashBoardResponse.Count(visitCount, totalCount, saleCount, salePrice, refundCount, refundPrice, newRegister);
    }

    private BooleanBuilder saleFilter() {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(ticketStateNe(TicketStateType.ADMIN_REFUND));
        builder.and(ticketStateNe(TicketStateType.USER_REFUND));
        builder.and(ticketTypeNe(TicketType.INVITE));
        return builder;
    }

    private BooleanBuilder refundFilter() {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(ticketStateEq(TicketStateType.ADMIN_REFUND).or(ticketStateEq(TicketStateType.USER_REFUND)));
        builder.and(ticketTypeNe(TicketType.INVITE));
        return builder;
    }


    private BooleanExpression ticketStartDateEq(LocalDate now) {
        return Expressions.dateTemplate(Date.class, "DATE({0})", QTicket.ticket.startDate).eq(Date.valueOf(now));
    }

    private BooleanExpression ticketUseDateEq(LocalDate now) {
        return Expressions.dateTemplate(Date.class, "DATE({0})", QTicket.ticket.usedDate).eq(Date.valueOf(now));
    }

    private BooleanExpression ticketCreateDateEq(LocalDate now) {
        return Expressions.dateTemplate(Date.class, "DATE({0})", QTicket.ticket.createdDate).eq(Date.valueOf(now));
    }

    private BooleanExpression accountCreatedDateEq(LocalDate now) {
        return Expressions.dateTemplate(Date.class, "DATE({0})", QAccount.account.createdDate).eq(Date.valueOf(now));
    }

    private BooleanExpression ticketStateEq(TicketStateType value) {
        return value != null ? QTicket.ticket.state.eq(value) : null;
    }

    private BooleanExpression ticketStateNe(TicketStateType value) {
        return value != null ? QTicket.ticket.state.ne(value) : null;
    }

    private BooleanExpression ticketTypeNe(TicketType value) {
        return value != null ? QTicket.ticket.type.ne(value) : null;
    }

    private BooleanExpression ticketKindNe(TicketKindType value) {
        return value != null ? QTicket.ticket.kind.ne(value) : null;
    }
}