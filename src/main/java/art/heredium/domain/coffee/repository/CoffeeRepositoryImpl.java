package art.heredium.domain.coffee.repository;

import art.heredium.domain.coffee.entity.QCoffee;
import art.heredium.domain.common.type.DateState;
import art.heredium.domain.common.type.HallType;
import art.heredium.domain.common.type.ProjectStateType;
import art.heredium.domain.ticket.entity.QTicket;
import art.heredium.domain.coffee.entity.Coffee;
import art.heredium.domain.coffee.model.dto.request.GetAdminCoffeeRequest;
import art.heredium.domain.coffee.model.dto.request.GetUserCoffeeRequest;
import art.heredium.domain.coffee.model.dto.response.GetAdminCoffeeResponse;
import art.heredium.domain.coffee.model.dto.response.QGetAdminCoffeeResponse;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.domain.ticket.type.TicketType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.tika.utils.StringUtils;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class CoffeeRepositoryImpl implements CoffeeRepositoryQueryDsl {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<GetAdminCoffeeResponse> search(GetAdminCoffeeRequest dto, Pageable pageable) {
        Long total = queryFactory
                .select(Wildcard.count)
                .from(QCoffee.coffee)
                .where(searchFilter(dto))
                .fetch()
                .get(0);

        NumberPath<Integer> totalBooking = Expressions.numberPath(Integer.class, "totalBooking");
        List<GetAdminCoffeeResponse> result = total > 0 ? queryFactory
                .select(new QGetAdminCoffeeResponse(QCoffee.coffee.id,
                        QCoffee.coffee.thumbnail,
                        QCoffee.coffee.title,
                        QCoffee.coffee.halls,
                        QCoffee.coffee.isEnabled,
                        QCoffee.coffee.startDate,
                        QCoffee.coffee.endDate,
                        QCoffee.coffee.bookingDate,
                        ExpressionUtils.as(JPAExpressions.select(QTicket.ticket.number.sum().coalesce(0))
                                        .from(QTicket.ticket)
                                        .where(totalBookingFilter(TicketKindType.COFFEE, QCoffee.coffee.id)),
                                totalBooking),
                        QCoffee.coffee.createdDate,
                        QCoffee.coffee.createdName,
                        QCoffee.coffee.lastModifiedDate,
                        QCoffee.coffee.lastModifiedName
                ))
                .from(QCoffee.coffee)
                .where(searchFilter(dto))
                .orderBy(QCoffee.coffee.startDate.desc(), QCoffee.coffee.endDate.desc(), QCoffee.coffee.lastModifiedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch() : new ArrayList<>();

        return new PageImpl<>(result, pageable, total);
    }

    @Override
    public Slice<Coffee> search(GetUserCoffeeRequest dto, Pageable pageable) {
        List<Coffee> content = getSearchQuery(dto)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public List<Coffee> searchByHome() {
        GetUserCoffeeRequest dto = new GetUserCoffeeRequest();
        dto.setStates(Arrays.asList(ProjectStateType.SCHEDULE, ProjectStateType.BOOKING, ProjectStateType.PROGRESS));
        return getSearchQuery(dto).fetch();
    }

    private JPAQuery<Coffee> getSearchQuery(GetUserCoffeeRequest dto) {
        DateTimeExpression<LocalDateTime> now = DateTimeExpression.currentTimestamp(LocalDateTime.class);
        NumberExpression<Integer> state = new CaseBuilder()
                .when(now.before(QCoffee.coffee.startDate).and(now.before(QCoffee.coffee.bookingDate)))
                .then(DateState.SCHEDULE.getCode())
                .when(now.after(QCoffee.coffee.endDate))
                .then(DateState.TERMINATION.getCode())
                .otherwise(DateState.PROGRESS.getCode());

        JPAQuery<Coffee> searchQuery = queryFactory
                .selectFrom(QCoffee.coffee)
                .where(searchFilter(dto))
                .orderBy(state.asc(), QCoffee.coffee.startDate.desc(), QCoffee.coffee.endDate.desc(), QCoffee.coffee.lastModifiedDate.desc());
        return searchQuery;
    }

    private BooleanBuilder searchFilter(GetAdminCoffeeRequest dto) {
        BooleanBuilder builder = new BooleanBuilder();
        if (dto.getSearchDateType().equals(GetAdminCoffeeRequest.SearchDateType.CREATED_DATE)) {
            builder.and(createdDateGoe(dto.getStartDate()));
            builder.and(createdDateLoe(dto.getEndDate()));
        } else if (dto.getSearchDateType().equals(GetAdminCoffeeRequest.SearchDateType.LAST_MODIFIED_DATE)) {
            builder.and(lastModifiedDateGoe(dto.getStartDate()));
            builder.and(lastModifiedDateLoe(dto.getEndDate()));
        } else if (dto.getSearchDateType().equals(GetAdminCoffeeRequest.SearchDateType.SCHEDULE)) {
            builder.and(startDateGoe(dto.getStartDate()));
            builder.and(endDateLoe(dto.getEndDate()));
        }

        builder.and(hallJsonSearch(dto.getHall()));
        builder.and(enabledEq(dto.getIsEnabled()));

        if (dto.getState() != null && dto.getState().size() > 0) {
            BooleanBuilder stateBuilder = new BooleanBuilder();
            dto.getState().forEach(state -> stateBuilder.or(stateEq(state)));
            builder.and(stateBuilder);
        }

        if (!StringUtils.isBlank(dto.getText())) {
            BooleanBuilder textBuilder = new BooleanBuilder();
            textBuilder.or(titleContain(dto.getText()));
            textBuilder.or(createdNameContain(dto.getText()));
            textBuilder.or(lastModifiedNameContain(dto.getText()));
            builder.and(textBuilder);
        }
        return builder;
    }

    private BooleanBuilder searchFilter(GetUserCoffeeRequest dto) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(enabledEq(true));

        BooleanBuilder orBuilder = new BooleanBuilder();
        dto.getStates().forEach(state -> orBuilder.or(stateEq(state)));
        builder.and(orBuilder);
        return builder;
    }

    private BooleanBuilder totalBookingFilter(TicketKindType kind, NumberPath<Long> kindId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(ticketKindEq(kind));
        builder.and(ticketKindIdEq(kindId));
        builder.and(ticketStateNe(TicketStateType.ADMIN_REFUND));
        builder.and(ticketStateNe(TicketStateType.USER_REFUND));
        builder.and(ticketTypeNe(TicketType.INVITE));
        return builder;
    }

    private BooleanExpression hallJsonSearch(HallType value) {
        return value != null ? Expressions.stringTemplate("JSON_SEARCH({0}, {1}, {2})", QCoffee.coffee.halls, "one", value.name()).isNotNull() : null;
    }

    private BooleanExpression titleContain(String value) {
        return !StringUtils.isBlank(value) ? QCoffee.coffee.title.contains(value) : null;
    }

    private BooleanExpression createdNameContain(String value) {
        return !StringUtils.isBlank(value) ? QCoffee.coffee.createdName.contains(value) : null;
    }

    private BooleanExpression lastModifiedNameContain(String value) {
        return !StringUtils.isBlank(value) ? QCoffee.coffee.lastModifiedName.contains(value) : null;
    }

    private BooleanExpression enabledEq(Boolean enabled) {
        return enabled != null ? QCoffee.coffee.isEnabled.eq(enabled) : null;
    }

    private BooleanExpression createdDateGoe(LocalDateTime value) {
        return value != null ? QCoffee.coffee.createdDate.goe(value) : null;
    }

    private BooleanExpression createdDateLoe(LocalDateTime value) {
        return value != null ? QCoffee.coffee.createdDate.loe(value) : null;
    }

    private BooleanExpression lastModifiedDateGoe(LocalDateTime value) {
        return value != null ? QCoffee.coffee.lastModifiedDate.goe(value) : null;
    }

    private BooleanExpression lastModifiedDateLoe(LocalDateTime value) {
        return value != null ? QCoffee.coffee.lastModifiedDate.loe(value) : null;
    }

    private BooleanExpression startDateGoe(LocalDateTime value) {
        return value != null ? QCoffee.coffee.startDate.goe(value) : null;
    }

    private BooleanExpression endDateLoe(LocalDateTime value) {
        return value != null ? QCoffee.coffee.endDate.loe(value) : null;
    }

    private BooleanBuilder stateEq(ProjectStateType state) {
        BooleanBuilder builder = new BooleanBuilder();
        DateTimeExpression<LocalDateTime> now = DateTimeExpression.currentTimestamp(LocalDateTime.class);
        if (state.equals(ProjectStateType.SCHEDULE)) {
            builder.and(now.before(QCoffee.coffee.startDate));
            builder.and(now.before(QCoffee.coffee.bookingDate));
        } else if (state.equals(ProjectStateType.BOOKING)) {
            builder.and(now.goe(QCoffee.coffee.bookingDate));
            builder.and(now.before(QCoffee.coffee.startDate));
        } else if (state.equals(ProjectStateType.PROGRESS)) {
            builder.and(now.between(QCoffee.coffee.startDate, QCoffee.coffee.endDate));
        } else if (state.equals(ProjectStateType.TERMINATION)) {
            builder.and(now.after(QCoffee.coffee.endDate));
        }
        return builder;
    }

    private BooleanExpression ticketKindEq(TicketKindType value) {
        return value != null ? QTicket.ticket.kind.eq(value) : null;
    }

    private BooleanExpression ticketKindIdEq(NumberPath<Long> value) {
        return value != null ? QTicket.ticket.kindId.eq(value) : null;
    }

    private BooleanExpression ticketTypeNe(TicketType value) {
        return value != null ? QTicket.ticket.type.ne(value) : null;
    }

    private BooleanExpression ticketStateNe(TicketStateType value) {
        return value != null ? QTicket.ticket.state.ne(value) : null;
    }
}