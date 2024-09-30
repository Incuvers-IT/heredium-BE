package art.heredium.domain.exhibition.repository;

import art.heredium.domain.common.type.DateState;
import art.heredium.domain.common.type.HallType;
import art.heredium.domain.common.type.ProjectStateType;
import art.heredium.domain.exhibition.entity.QExhibition;
import art.heredium.domain.exhibition.model.dto.response.QGetAdminExhibitionResponse;
import art.heredium.domain.ticket.entity.QTicket;
import art.heredium.domain.exhibition.entity.Exhibition;
import art.heredium.domain.exhibition.model.dto.request.GetAdminExhibitionRequest;
import art.heredium.domain.exhibition.model.dto.request.GetUserExhibitionRequest;
import art.heredium.domain.exhibition.model.dto.response.GetAdminExhibitionResponse;
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
public class ExhibitionRepositoryImpl implements ExhibitionRepositoryQueryDsl {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<GetAdminExhibitionResponse> search(GetAdminExhibitionRequest dto, Pageable pageable) {
        Long total = queryFactory
                .select(Wildcard.count)
                .from(QExhibition.exhibition)
                .where(searchFilter(dto))
                .fetch()
                .get(0);

        NumberPath<Integer> totalBooking = Expressions.numberPath(Integer.class, "totalBooking");
        List<GetAdminExhibitionResponse> result = total > 0 ? queryFactory
                .select(new QGetAdminExhibitionResponse(QExhibition.exhibition.id,
                        QExhibition.exhibition.thumbnail,
                        QExhibition.exhibition.title,
                        QExhibition.exhibition.halls,
                        QExhibition.exhibition.isEnabled,
                        QExhibition.exhibition.startDate,
                        QExhibition.exhibition.endDate,
                        QExhibition.exhibition.bookingDate,
                        ExpressionUtils.as(JPAExpressions.select(QTicket.ticket.number.sum().coalesce(0))
                                        .from(QTicket.ticket)
                                        .where(totalBookingFilter(TicketKindType.EXHIBITION, QExhibition.exhibition.id)),
                                totalBooking),
                        QExhibition.exhibition.createdDate,
                        QExhibition.exhibition.createdName,
                        QExhibition.exhibition.lastModifiedDate,
                        QExhibition.exhibition.lastModifiedName
                ))
                .from(QExhibition.exhibition)
                .where(searchFilter(dto))
                .orderBy(QExhibition.exhibition.startDate.desc(), QExhibition.exhibition.endDate.desc(), QExhibition.exhibition.lastModifiedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch() : new ArrayList<>();

        return new PageImpl<>(result, pageable, total);
    }

    @Override
    public Slice<Exhibition> search(GetUserExhibitionRequest dto, Pageable pageable) {
        List<Exhibition> content = getSearchQuery(dto)
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
    public List<Exhibition> searchByHome() {
        GetUserExhibitionRequest dto = new GetUserExhibitionRequest();
        dto.setStates(Arrays.asList(ProjectStateType.SCHEDULE, ProjectStateType.BOOKING, ProjectStateType.PROGRESS));
        return getSearchQuery(dto).fetch();
    }

    private JPAQuery<Exhibition> getSearchQuery(GetUserExhibitionRequest dto) {
        DateTimeExpression<LocalDateTime> now = DateTimeExpression.currentTimestamp(LocalDateTime.class);
        NumberExpression<Integer> state = new CaseBuilder()
                .when(now.before(QExhibition.exhibition.startDate).and(now.before(QExhibition.exhibition.bookingDate)))
                .then(DateState.SCHEDULE.getCode())
                .when(now.after(QExhibition.exhibition.endDate))
                .then(DateState.TERMINATION.getCode())
                .otherwise(DateState.PROGRESS.getCode());

        JPAQuery<Exhibition> searchQuery = queryFactory
                .selectFrom(QExhibition.exhibition)
                .where(searchFilter(dto))
                .orderBy(state.asc(), QExhibition.exhibition.startDate.desc(), QExhibition.exhibition.endDate.desc(), QExhibition.exhibition.lastModifiedDate.desc());
        return searchQuery;
    }

    private BooleanBuilder searchFilter(GetAdminExhibitionRequest dto) {
        BooleanBuilder builder = new BooleanBuilder();
        if (dto.getSearchDateType().equals(GetAdminExhibitionRequest.SearchDateType.CREATED_DATE)) {
            builder.and(createdDateGoe(dto.getStartDate()));
            builder.and(createdDateLoe(dto.getEndDate()));
        } else if (dto.getSearchDateType().equals(GetAdminExhibitionRequest.SearchDateType.LAST_MODIFIED_DATE)) {
            builder.and(lastModifiedDateGoe(dto.getStartDate()));
            builder.and(lastModifiedDateLoe(dto.getEndDate()));
        } else if (dto.getSearchDateType().equals(GetAdminExhibitionRequest.SearchDateType.SCHEDULE)) {
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

    private BooleanBuilder searchFilter(GetUserExhibitionRequest dto) {
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
        return value != null ? Expressions.stringTemplate("JSON_SEARCH({0}, {1}, {2})", QExhibition.exhibition.halls, "one", value.name()).isNotNull() : null;
    }

    private BooleanExpression titleContain(String value) {
        return !StringUtils.isBlank(value) ? QExhibition.exhibition.title.contains(value) : null;
    }

    private BooleanExpression createdNameContain(String value) {
        return !StringUtils.isBlank(value) ? QExhibition.exhibition.createdName.contains(value) : null;
    }

    private BooleanExpression lastModifiedNameContain(String value) {
        return !StringUtils.isBlank(value) ? QExhibition.exhibition.lastModifiedName.contains(value) : null;
    }

    private BooleanExpression enabledEq(Boolean enabled) {
        return enabled != null ? QExhibition.exhibition.isEnabled.eq(enabled) : null;
    }

    private BooleanExpression createdDateGoe(LocalDateTime value) {
        return value != null ? QExhibition.exhibition.createdDate.goe(value) : null;
    }

    private BooleanExpression createdDateLoe(LocalDateTime value) {
        return value != null ? QExhibition.exhibition.createdDate.loe(value) : null;
    }

    private BooleanExpression lastModifiedDateGoe(LocalDateTime value) {
        return value != null ? QExhibition.exhibition.lastModifiedDate.goe(value) : null;
    }

    private BooleanExpression lastModifiedDateLoe(LocalDateTime value) {
        return value != null ? QExhibition.exhibition.lastModifiedDate.loe(value) : null;
    }

    private BooleanExpression startDateGoe(LocalDateTime value) {
        return value != null ? QExhibition.exhibition.startDate.goe(value) : null;
    }

    private BooleanExpression endDateLoe(LocalDateTime value) {
        return value != null ? QExhibition.exhibition.endDate.loe(value) : null;
    }

    private BooleanBuilder stateEq(ProjectStateType state) {
        BooleanBuilder builder = new BooleanBuilder();
        DateTimeExpression<LocalDateTime> now = DateTimeExpression.currentTimestamp(LocalDateTime.class);
        if (state.equals(ProjectStateType.SCHEDULE)) {
            builder.and(now.before(QExhibition.exhibition.startDate));
            builder.and(now.before(QExhibition.exhibition.bookingDate));
        } else if (state.equals(ProjectStateType.BOOKING)) {
            builder.and(now.goe(QExhibition.exhibition.bookingDate));
            builder.and(now.before(QExhibition.exhibition.startDate));
        } else if (state.equals(ProjectStateType.PROGRESS)) {
            builder.and(now.between(QExhibition.exhibition.startDate, QExhibition.exhibition.endDate));
        } else if (state.equals(ProjectStateType.TERMINATION)) {
            builder.and(now.after(QExhibition.exhibition.endDate));
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