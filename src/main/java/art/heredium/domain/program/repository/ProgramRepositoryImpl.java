package art.heredium.domain.program.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.*;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.tika.utils.StringUtils;

import art.heredium.domain.common.type.DateState;
import art.heredium.domain.common.type.HallType;
import art.heredium.domain.common.type.ProjectStateType;
import art.heredium.domain.program.entity.Program;
import art.heredium.domain.program.entity.QProgram;
import art.heredium.domain.program.model.dto.request.GetAdminProgramRequest;
import art.heredium.domain.program.model.dto.request.GetUserProgramRequest;
import art.heredium.domain.program.model.dto.response.GetAdminProgramResponse;
import art.heredium.domain.program.model.dto.response.QGetAdminProgramResponse;
import art.heredium.domain.ticket.entity.QTicket;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.domain.ticket.type.TicketType;

@RequiredArgsConstructor
public class ProgramRepositoryImpl implements ProgramRepositoryQueryDsl {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<GetAdminProgramResponse> search(GetAdminProgramRequest dto, Pageable pageable) {
    Long total =
        queryFactory
            .select(Wildcard.count)
            .from(QProgram.program)
            .where(searchFilter(dto))
            .fetch()
            .get(0);

    NumberPath<Integer> totalBooking = Expressions.numberPath(Integer.class, "totalBooking");
    List<GetAdminProgramResponse> result =
        total > 0
            ? queryFactory
                .select(
                    new QGetAdminProgramResponse(
                        QProgram.program.id,
                        QProgram.program.thumbnail,
                        QProgram.program.title,
                        QProgram.program.halls,
                        QProgram.program.isEnabled,
                        QProgram.program.startDate,
                        QProgram.program.endDate,
                        QProgram.program.bookingDate,
                        ExpressionUtils.as(
                            JPAExpressions.select(QTicket.ticket.number.sum().coalesce(0))
                                .from(QTicket.ticket)
                                .where(
                                    totalBookingFilter(
                                        TicketKindType.PROGRAM, QProgram.program.id)),
                            totalBooking),
                        QProgram.program.createdDate,
                        QProgram.program.createdName,
                        QProgram.program.lastModifiedDate,
                        QProgram.program.lastModifiedName))
                .from(QProgram.program)
                .where(searchFilter(dto))
                .orderBy(
                    QProgram.program.startDate.desc(),
                    QProgram.program.endDate.desc(),
                    QProgram.program.lastModifiedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
            : new ArrayList<>();

    return new PageImpl<>(result, pageable, total);
  }

  @Override
  public Slice<Program> search(GetUserProgramRequest dto, Pageable pageable) {
    List<Program> content =
        getSearchQuery(dto).offset(pageable.getOffset()).limit(pageable.getPageSize() + 1).fetch();

    boolean hasNext = false;
    if (content.size() > pageable.getPageSize()) {
      content.remove(pageable.getPageSize());
      hasNext = true;
    }

    return new SliceImpl<>(content, pageable, hasNext);
  }

  @Override
  public List<Program> searchByHome() {
    GetUserProgramRequest dto = new GetUserProgramRequest();
    dto.setStates(
        Arrays.asList(
            ProjectStateType.SCHEDULE, ProjectStateType.BOOKING, ProjectStateType.PROGRESS));
    return getSearchQuery(dto).fetch();
  }

  private JPAQuery<Program> getSearchQuery(GetUserProgramRequest dto) {
    DateTimeExpression<LocalDateTime> now =
        DateTimeExpression.currentTimestamp(LocalDateTime.class);
    NumberExpression<Integer> state =
        new CaseBuilder()
            .when(
                now.before(QProgram.program.startDate)
                    .and(now.before(QProgram.program.bookingDate)))
            .then(DateState.SCHEDULE.getCode())
            .when(now.after(QProgram.program.endDate))
            .then(DateState.TERMINATION.getCode())
            .otherwise(DateState.PROGRESS.getCode());

    JPAQuery<Program> searchQuery =
        queryFactory
            .selectFrom(QProgram.program)
            .where(searchFilter(dto))
            .orderBy(
                state.asc(),
                QProgram.program.startDate.desc(),
                QProgram.program.endDate.desc(),
                QProgram.program.lastModifiedDate.desc());
    return searchQuery;
  }

  private BooleanBuilder searchFilter(GetAdminProgramRequest dto) {
    BooleanBuilder builder = new BooleanBuilder();
    if (dto.getSearchDateType().equals(GetAdminProgramRequest.SearchDateType.CREATED_DATE)) {
      builder.and(createdDateGoe(dto.getStartDate()));
      builder.and(createdDateLoe(dto.getEndDate()));
    } else if (dto.getSearchDateType()
        .equals(GetAdminProgramRequest.SearchDateType.LAST_MODIFIED_DATE)) {
      builder.and(lastModifiedDateGoe(dto.getStartDate()));
      builder.and(lastModifiedDateLoe(dto.getEndDate()));
    } else if (dto.getSearchDateType().equals(GetAdminProgramRequest.SearchDateType.SCHEDULE)) {
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

  private BooleanBuilder searchFilter(GetUserProgramRequest dto) {
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
    return value != null
        ? Expressions.stringTemplate(
                "JSON_SEARCH({0}, {1}, {2})", QProgram.program.halls, "one", value.name())
            .isNotNull()
        : null;
  }

  private BooleanExpression titleContain(String value) {
    return !StringUtils.isBlank(value) ? QProgram.program.title.contains(value) : null;
  }

  private BooleanExpression createdNameContain(String value) {
    return !StringUtils.isBlank(value) ? QProgram.program.createdName.contains(value) : null;
  }

  private BooleanExpression lastModifiedNameContain(String value) {
    return !StringUtils.isBlank(value) ? QProgram.program.lastModifiedName.contains(value) : null;
  }

  private BooleanExpression enabledEq(Boolean enabled) {
    return enabled != null ? QProgram.program.isEnabled.eq(enabled) : null;
  }

  private BooleanExpression createdDateGoe(LocalDateTime value) {
    return value != null ? QProgram.program.createdDate.goe(value) : null;
  }

  private BooleanExpression createdDateLoe(LocalDateTime value) {
    return value != null ? QProgram.program.createdDate.loe(value) : null;
  }

  private BooleanExpression lastModifiedDateGoe(LocalDateTime value) {
    return value != null ? QProgram.program.lastModifiedDate.goe(value) : null;
  }

  private BooleanExpression lastModifiedDateLoe(LocalDateTime value) {
    return value != null ? QProgram.program.lastModifiedDate.loe(value) : null;
  }

  private BooleanExpression startDateGoe(LocalDateTime value) {
    return value != null ? QProgram.program.startDate.goe(value) : null;
  }

  private BooleanExpression endDateLoe(LocalDateTime value) {
    return value != null ? QProgram.program.endDate.loe(value) : null;
  }

  private BooleanBuilder stateEq(ProjectStateType state) {
    BooleanBuilder builder = new BooleanBuilder();
    DateTimeExpression<LocalDateTime> now =
        DateTimeExpression.currentTimestamp(LocalDateTime.class);
    if (state.equals(ProjectStateType.SCHEDULE)) {
      builder.and(now.before(QProgram.program.startDate));
      builder.and(now.before(QProgram.program.bookingDate));
    } else if (state.equals(ProjectStateType.BOOKING)) {
      builder.and(now.goe(QProgram.program.bookingDate));
      builder.and(now.before(QProgram.program.startDate));
    } else if (state.equals(ProjectStateType.PROGRESS)) {
      builder.and(now.between(QProgram.program.startDate, QProgram.program.endDate));
    } else if (state.equals(ProjectStateType.TERMINATION)) {
      builder.and(now.after(QProgram.program.endDate));
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
