package art.heredium.domain.statistics.repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;

import art.heredium.domain.account.entity.QAccount;
import art.heredium.domain.common.model.dto.response.ChartResponse;
import art.heredium.domain.common.model.dto.response.QChartResponse;
import art.heredium.domain.common.type.ProjectPriceType;
import art.heredium.domain.statistics.model.dto.response.GetAdminStatisticsSummaryResponse;
import art.heredium.domain.statistics.model.dto.response.QGetAdminStatisticsSummaryResponse_TicketPriceInfo;
import art.heredium.domain.statistics.type.StatisticsDateType;
import art.heredium.domain.statistics.type.StatisticsType;
import art.heredium.domain.ticket.entity.QTicket;
import art.heredium.domain.ticket.entity.QTicketPrice;
import art.heredium.domain.ticket.model.dto.response.GetAdminTicketStatisticsDashboardResponse;
import art.heredium.domain.ticket.model.dto.response.QGetAdminTicketStatisticsDashboardResponse;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.domain.ticket.type.TicketType;

@Repository
@RequiredArgsConstructor
public class StatisticsRepository {

  private final JPAQueryFactory queryFactory;

  public GetAdminTicketStatisticsDashboardResponse dashboard(
      TicketKindType kind, Long kindId, String ticketPriceType) {
    return queryFactory
        .select(
            new QGetAdminTicketStatisticsDashboardResponse(
                Expressions.asString(ticketPriceType != null ? ticketPriceType : ""),
                QTicketPrice.ticketPrice.number.sum().coalesce(0),
                QTicketPrice.ticketPrice.price.sum().coalesce(0L)))
        .from(QTicket.ticket)
        .innerJoin(QTicket.ticket.prices, QTicketPrice.ticketPrice)
        .where(dashBoardFilter(kind, kindId, ticketPriceType))
        .fetchOne();
  }

  public List<GetAdminStatisticsSummaryResponse.TicketPriceInfo> ticketPriceInfo(
      LocalDateTime startDate, LocalDateTime endDate, TicketKindType kind, Long kindId) {
    NumberPath<Long> aliasUnitPrice = Expressions.numberPath(Long.class, "unitPrice");

    NumberExpression<Long> unitPrice =
        new CaseBuilder()
            .when(QTicket.ticket.type.eq(TicketType.NORMAL))
            .then(QTicketPrice.ticketPrice.price)
            .otherwise(Expressions.nullExpression());

    NumberExpression<Integer> refundNumber =
        new CaseBuilder()
            .when(
                QTicket.ticket.state.in(TicketStateType.ADMIN_REFUND, TicketStateType.USER_REFUND))
            .then(QTicketPrice.ticketPrice.number)
            .otherwise(0);

    NumberExpression<Long> totalPrice =
        new CaseBuilder()
            .when(
                QTicket.ticket.state.notIn(
                    TicketStateType.ADMIN_REFUND, TicketStateType.USER_REFUND))
            .then(QTicketPrice.ticketPrice.price.multiply(QTicketPrice.ticketPrice.number))
            .otherwise(0L);

    List<GetAdminStatisticsSummaryResponse.TicketPriceInfo> fetch =
        queryFactory
            .select(
                new QGetAdminStatisticsSummaryResponse_TicketPriceInfo(
                    QTicketPrice.ticketPrice.type,
                    unitPrice.as(aliasUnitPrice),
                    QTicketPrice.ticketPrice.number.sum(),
                    refundNumber.sum(),
                    totalPrice.sum()))
            .from(QTicket.ticket)
            .innerJoin(QTicket.ticket.prices, QTicketPrice.ticketPrice)
            .where(
                ticketStartDateBetween(startDate, endDate),
                ticketKindEq(kind),
                ticketKindIdEq(kindId))
            .groupBy(QTicketPrice.ticketPrice.type, aliasUnitPrice)
            .fetch();

    return fetch.stream()
        .sorted(
            Comparator.comparing(
                    GetAdminStatisticsSummaryResponse.TicketPriceInfo::getUnitPrice,
                    (a, b) -> {
                      if (a == null && b == null) return 0;
                      else if (a == null) return 1;
                      else if (b == null) return -1;
                      else return 0;
                    })
                .thenComparing(
                    x -> ProjectPriceType.getCodeOfDesc(x.getType()),
                    Comparator.nullsLast(Comparator.naturalOrder())))
        .collect(Collectors.toList());
  }

  public List<ChartResponse> ticketInfoChart(
      LocalDateTime startDate, LocalDateTime endDate, TicketKindType kind, Long kindId) {
    StringPath label = Expressions.stringPath("label");
    NumberPath<Double> num = Expressions.numberPath(Double.class, "num");
    List<String> projectPriceTypes =
        Arrays.stream(ProjectPriceType.values())
            .map(ProjectPriceType::getDesc)
            .collect(Collectors.toList());
    StringExpression typeCase =
        new CaseBuilder()
            .when(QTicketPrice.ticketPrice.type.in(projectPriceTypes))
            .then(QTicketPrice.ticketPrice.type)
            .otherwise("기타");
    return queryFactory
        .select(
            new QChartResponse(
                typeCase.as(label),
                QTicketPrice.ticketPrice.number.castToNum(Double.class).sum().as(num)))
        .from(QTicket.ticket)
        .innerJoin(QTicket.ticket.prices, QTicketPrice.ticketPrice)
        .where(
            ticketStartDateBetween(startDate, endDate),
            ticketStateNe(TicketStateType.ADMIN_REFUND),
            ticketStateNe(TicketStateType.USER_REFUND),
            ticketKindEq(kind),
            ticketKindIdEq(kindId))
        .groupBy(label)
        .orderBy(num.desc())
        .fetch();
  }

  public List<ChartResponse> chart(
      LocalDateTime startDate,
      LocalDateTime endDate,
      TicketKindType kind,
      Long kindId,
      StatisticsType type,
      StatisticsDateType dateType) {

    StringPath label = Expressions.stringPath("label");
    if (type.equals(StatisticsType.SIGN_UP)) {
      return queryFactory
          .select(
              new QChartResponse(
                  getDateFormat(QAccount.account.createdDate, dateType.getFormat()).as(label),
                  Wildcard.count.castToNum(Double.class)))
          .from(QAccount.account)
          .where(accountCratedDateBetween(startDate, endDate))
          .groupBy(label)
          .orderBy(label.asc())
          .fetch();
    } else if (type == StatisticsType.COME) {
      return queryFactory
          .select(
              new QChartResponse(
                  getDateFormat(QTicket.ticket.usedDate, dateType.getFormat()).as(label),
                  QTicket.ticket.number.sum().castToNum(Double.class)))
          .from(QTicket.ticket)
          .where(chartFilter(startDate, endDate, kind, kindId, type))
          .groupBy(label)
          .orderBy(label.asc())
          .fetch();
    } else {
      return queryFactory
          .select(
              new QChartResponse(
                  getDateFormat(QTicket.ticket.createdDate, dateType.getFormat()).as(label),
                  QTicket.ticket.price.sum().castToNum(Double.class)))
          .from(QTicket.ticket)
          .where(chartFilter(startDate, endDate, kind, kindId, type))
          .groupBy(label)
          .orderBy(label.asc())
          .fetch();
    }
  }

  private BooleanBuilder chartFilter(
      LocalDateTime startDate,
      LocalDateTime endDate,
      TicketKindType kind,
      Long kindId,
      StatisticsType type) {
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(ticketKindEq(kind));
    builder.and(ticketKindIdEq(kindId));
    if (type.equals(StatisticsType.COME)) {
      builder.and(ticketUseDateBetween(startDate, endDate));
      builder.and(ticketStateEq(TicketStateType.USED));
    } else if (type.equals(StatisticsType.PRICE)) {
      builder.and(ticketCreatedDateBetween(startDate, endDate));
      builder.and(ticketStateNe(TicketStateType.ADMIN_REFUND));
      builder.and(ticketStateNe(TicketStateType.USER_REFUND));
      builder.and(ticketTypeNe(TicketType.INVITE));
    }
    return builder;
  }

  private BooleanBuilder dashBoardFilter(TicketKindType kind, Long kindId, String ticketPriceType) {
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(ticketKindEq(kind));
    builder.and(ticketKindIdEq(kindId));
    builder.and(ticketPriceType(ticketPriceType));
    builder.and(ticketStateNe(TicketStateType.ADMIN_REFUND));
    builder.and(ticketStateNe(TicketStateType.USER_REFUND));
    builder.and(ticketTypeNe(TicketType.INVITE));
    return builder;
  }

  private BooleanExpression ticketKindEq(TicketKindType value) {
    return value != null ? QTicket.ticket.kind.eq(value) : null;
  }

  private BooleanExpression ticketKindIdEq(Long value) {
    return value != null ? QTicket.ticket.kindId.eq(value) : null;
  }

  private BooleanExpression ticketTypeNe(TicketType value) {
    return value != null ? QTicket.ticket.type.ne(value) : null;
  }

  private BooleanExpression ticketPriceType(String value) {
    return value != null ? QTicketPrice.ticketPrice.type.eq(value) : null;
  }

  private BooleanExpression ticketStateNe(TicketStateType value) {
    return value != null ? QTicket.ticket.state.ne(value) : null;
  }

  private BooleanExpression ticketStateEq(TicketStateType value) {
    return value != null ? QTicket.ticket.state.eq(value) : null;
  }

  private BooleanExpression ticketStartDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return startDate != null && endDate != null
        ? QTicket.ticket.startDate.between(startDate, endDate)
        : null;
  }

  private BooleanExpression ticketUseDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
    return startDate != null && endDate != null
        ? QTicket.ticket.usedDate.between(startDate, endDate)
        : null;
  }

  private BooleanExpression ticketCreatedDateBetween(
      LocalDateTime startDate, LocalDateTime endDate) {
    return startDate != null && endDate != null
        ? QTicket.ticket.createdDate.between(startDate, endDate)
        : null;
  }

  private BooleanExpression accountCratedDateBetween(
      LocalDateTime startDate, LocalDateTime endDate) {
    return startDate != null && endDate != null
        ? QAccount.account.createdDate.between(startDate, endDate)
        : null;
  }

  private StringTemplate getDateFormat(DateTimePath<LocalDateTime> dateField, String format) {
    return Expressions.stringTemplate(
        "DATE_FORMAT({0}, {1})", dateField, ConstantImpl.create(format));
  }
}
