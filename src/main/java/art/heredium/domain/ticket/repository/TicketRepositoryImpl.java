package art.heredium.domain.ticket.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.tika.utils.StringUtils;

import art.heredium.core.util.Constants;
import art.heredium.domain.account.model.dto.request.GetUserNonUserTicketRequest;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.ticket.entity.QTicketPrice;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.model.dto.request.GetAdminTicketRequest;
import art.heredium.domain.ticket.model.dto.response.GetUserMemberTicketEnabledResponse;
import art.heredium.domain.ticket.model.dto.response.GetUserMemberTicketResponse;
import art.heredium.domain.ticket.model.dto.response.QGetUserMemberTicketEnabledResponse;
import art.heredium.domain.ticket.model.dto.response.QGetUserMemberTicketResponse;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.domain.ticket.type.TicketType;

import static art.heredium.domain.account.entity.QAccount.account;
import static art.heredium.domain.account.entity.QNonUser.nonUser;
import static art.heredium.domain.coffee.entity.QCoffee.coffee;
import static art.heredium.domain.exhibition.entity.QExhibition.exhibition;
import static art.heredium.domain.membership.entity.QMembershipRegistration.membershipRegistration;
import static art.heredium.domain.program.entity.QProgram.program;
import static art.heredium.domain.ticket.entity.QTicket.ticket;

@RequiredArgsConstructor
public class TicketRepositoryImpl implements TicketRepositoryQueryDsl {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Ticket> search(GetAdminTicketRequest dto, Pageable pageable) {
    BooleanBuilder whereClause = new BooleanBuilder(searchFilter(dto));

    if (dto.getHasMembership() != null) {
      if (dto.getHasMembership()) {
        // Tickets associated with accounts that have membership
        whereClause
            .and(ticket.account.isNotNull())
            .and(
                ticket.account.id.in(
                    JPAExpressions.select(membershipRegistration.account.id)
                        .from(membershipRegistration)
//                        .where(membershipRegistration.expirationDate.goe(LocalDateTime.now()))
                )
            );
      } else {
        // Tickets associated with accounts without membership, or non-users, or neither
        whereClause.and(
            ticket
                .account
                .isNull()
                .or(
                    ticket
                        .account
                        .isNotNull()
                        .and(
                            ticket.account.id.notIn(
                                JPAExpressions.select(membershipRegistration.account.id)
                                    .from(membershipRegistration))))
                .or(ticket.nonUser.isNotNull()));
      }
    }

    long total =
        queryFactory
            .select(ticket.count())
            .from(ticket)
            .leftJoin(ticket.account, account)
            .leftJoin(ticket.nonUser, nonUser)
            .where(whereClause)
            .fetchOne();

    List<Ticket> results =
        queryFactory
            .selectFrom(ticket)
            .leftJoin(ticket.account, account)
            .fetchJoin()
            .leftJoin(ticket.nonUser, nonUser)
            .fetchJoin()
            .where(whereClause)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(ticket.createdDate.desc())
            .fetch();

    return new PageImpl<>(results, pageable, total);
  }

  @Override
  public List<Ticket> search(GetAdminTicketRequest dto) {
    BooleanBuilder whereClause = new BooleanBuilder(searchFilter(dto));

    if (dto.getHasMembership() != null) {
      if (dto.getHasMembership()) {
        whereClause
            .and(ticket.account.isNotNull())
            .and(
                ticket.account.id.in(
                    JPAExpressions.select(membershipRegistration.account.id)
                        .from(membershipRegistration)
//                        .where(membershipRegistration.expirationDate.goe(LocalDateTime.now()))
                ));
      } else {
        whereClause.and(
            ticket
                .account
                .isNull()
                .or(
                    ticket
                        .account
                        .isNotNull()
                        .and(
                            ticket.account.id.notIn(
                                JPAExpressions.select(membershipRegistration.account.id)
                                    .from(membershipRegistration))))
                .or(ticket.nonUser.isNotNull()));
      }
    }

    return queryFactory
        .selectFrom(ticket)
        .leftJoin(ticket.account, account)
        .fetchJoin()
        .leftJoin(ticket.nonUser, nonUser)
        .fetchJoin()
        .where(whereClause)
        .orderBy(ticket.createdDate.desc())
        .fetch();
  }

  @Override
  public Page<GetUserMemberTicketResponse> findByMember(
      Long accountId, List<TicketKindType> kinds, Integer year, Pageable pageable) {
    BooleanBuilder byMemberFilter = findByMemberFilter(accountId, kinds, year);
    return getGetUserMemberTicketResponses(pageable, byMemberFilter);
  }

  @Override
  public Page<GetUserMemberTicketResponse> findByNonUser(
      GetUserNonUserTicketRequest dto, Pageable pageable) {
    BooleanBuilder byNonUserFilter = findByNonUserFilter(dto);
    return getGetUserMemberTicketResponses(pageable, byNonUserFilter);
  }

  @Override
  public Page<GetUserMemberTicketResponse> findByNonUser(Long nonUserId, Pageable pageable) {
    BooleanBuilder byNonUserFilter = findByNonUserFilter(nonUserId);
    return getGetUserMemberTicketResponses(pageable, byNonUserFilter);
  }

  private PageImpl<GetUserMemberTicketResponse> getGetUserMemberTicketResponses(
      Pageable pageable, BooleanBuilder byNonUserFilter) {
    JPAQuery<?> jpaQuery =
        queryFactory
            .from(ticket)
            .leftJoin(exhibition)
            .on(ticket.kindId.eq(exhibition.id).and(ticket.kind.eq(TicketKindType.EXHIBITION)))
            .leftJoin(program)
            .on(ticket.kindId.eq(program.id).and(ticket.kind.eq(TicketKindType.PROGRAM)))
            .leftJoin(coffee)
            .on(ticket.kindId.eq(coffee.id).and(ticket.kind.eq(TicketKindType.COFFEE)))
            .leftJoin(ticket.nonUser, nonUser)
            .where(byNonUserFilter);

    Long total = jpaQuery.select(Wildcard.count).fetch().get(0);

    List<GetUserMemberTicketResponse> result =
        total > 0
            ? jpaQuery
                .select(
                    new QGetUserMemberTicketResponse(
                        ticket.id,
                        Expressions.template(
                            Storage.class,
                            "COALESCE({0}, {1}, {2})",
                            exhibition.thumbnail,
                            program.thumbnail,
                            coffee.thumbnail),
                        ticket.title,
                        ticket.uuid,
                        ticket.kind,
                        ticket.state,
                        ticket.startDate,
                        ticket.endDate,
                        getPriceInfo()))
                .orderBy(ticket.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
            : new ArrayList<>();

    return new PageImpl<>(result, pageable, total);
  }

  @Override
  public List<GetUserMemberTicketEnabledResponse> ticketByAccountAndEnabled(Long id) {
    TicketStateType[] states = {TicketStateType.PAYMENT, TicketStateType.ISSUANCE};
    NumberExpression<Integer> typeSort =
        new CaseBuilder().when(ticket.type.eq(TicketType.INVITE)).then(1).otherwise(0);
    return queryFactory
        .from(ticket)
        .select(
            new QGetUserMemberTicketEnabledResponse(
                ticket.id,
                ticket.title,
                ticket.kind,
                ticket.type,
                ticket.startDate,
                ticket.endDate,
                ticket.uuid,
                getPriceInfo()))
        .where(ticket.account.id.eq(id), stateIn(Arrays.asList(states)), ticketToday())
        .orderBy(typeSort.asc(), ticket.startDate.asc())
        .fetch();
  }

  private JPQLQuery<String> getPriceInfo() {
    return JPAExpressions.select(
            Expressions.stringTemplate(
                "GROUP_CONCAT_SEPARATOR({0}, {1})",
                Expressions.stringTemplate(
                    "CONCAT({0}, '=-=-=-=', {1})",
                    QTicketPrice.ticketPrice.type, QTicketPrice.ticketPrice.number),
                "=-,-="))
        .from(QTicketPrice.ticketPrice)
        .where(QTicketPrice.ticketPrice.ticket.id.eq(ticket.id))
        .groupBy(QTicketPrice.ticketPrice.ticket.id);
  }

  private BooleanBuilder searchFilter(GetAdminTicketRequest dto) {
    BooleanBuilder builder = new BooleanBuilder();
    if (dto.getSearchDateType().equals(GetAdminTicketRequest.SearchDateType.CREATED_DATE)) {
      builder.and(createdDateGoe(dto.getStartDate()));
      builder.and(createdDateLoe(dto.getEndDate()));
    } else if (dto.getSearchDateType().equals(GetAdminTicketRequest.SearchDateType.START_DATE)) {
      builder.and(startDateGoe(dto.getStartDate()));
      builder.and(startDateLoe(dto.getEndDate()));
    } else if (dto.getSearchDateType().equals(GetAdminTicketRequest.SearchDateType.USED_DATE)) {
      builder.and(usedDateGoe(dto.getStartDate()));
      builder.and(usedDateLoe(dto.getEndDate()));
    }

    builder.and(kindIn(dto.getKinds()));
    builder.and(typeEq(dto.getType()));
    builder.and(stateIn(dto.getState()));

    if (!StringUtils.isBlank(dto.getText())) {
      BooleanBuilder textBuilder = new BooleanBuilder();
      textBuilder.or(titleContain(dto.getText()));
      textBuilder.or(emailContain(dto.getText()));
      textBuilder.or(nameContain(dto.getText()));
      textBuilder.or(phoneContain(dto.getText()));
      textBuilder.or(uuidContain(dto.getText()));
      textBuilder.or(pgIdContain(dto.getText()));
      builder.and(textBuilder);
    }
    return builder;
  }

  private BooleanBuilder findByMemberFilter(
      Long accountId, List<TicketKindType> kinds, Integer year) {
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(ticket.account.id.eq(accountId));
    builder.and(kindIn(kinds));
    if (year == null) {
      LocalDateTime now = Constants.getNow();
      LocalDateTime beforeOneMonth = now.plusMonths(-1);
      builder.and(createdDateBetween(beforeOneMonth, now));
    } else {
      builder.and(createdDateYearEq(year));
    }
    return builder;
  }

  private BooleanBuilder findByNonUserFilter(GetUserNonUserTicketRequest dto) {
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(nonUser.phone.eq(dto.getPhone()));
    builder.and(nonUser.name.eq(dto.getName()));
    builder.and(ticket.password.eq(dto.getPassword()));
    builder.and(ticket.name.ne("탈퇴한 계정"));
    return builder;
  }

  private BooleanBuilder findByNonUserFilter(Long nonUserId) {
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(ticket.nonUser.id.eq(nonUserId));
    builder.and(ticket.name.ne("탈퇴한 계정"));
    return builder;
  }

  private BooleanExpression titleContain(String value) {
    return !StringUtils.isBlank(value) ? ticket.title.contains(value) : null;
  }

  private BooleanExpression emailContain(String value) {
    return !StringUtils.isBlank(value) ? ticket.email.contains(value) : null;
  }

  private BooleanExpression nameContain(String value) {
    return !StringUtils.isBlank(value) ? ticket.name.contains(value) : null;
  }

  private BooleanExpression phoneContain(String value) {
    return !StringUtils.isBlank(value) ? ticket.phone.contains(value) : null;
  }

  private BooleanExpression uuidContain(String value) {
    return !StringUtils.isBlank(value) ? ticket.uuid.contains(value) : null;
  }

  private BooleanExpression pgIdContain(String value) {
    return !StringUtils.isBlank(value) ? ticket.pgId.contains(value) : null;
  }

  private BooleanExpression createdDateGoe(LocalDateTime value) {
    return value != null ? ticket.createdDate.goe(value) : null;
  }

  private BooleanExpression createdDateLoe(LocalDateTime value) {
    return value != null ? ticket.createdDate.loe(value) : null;
  }

  private BooleanExpression createdDateBetween(LocalDateTime start, LocalDateTime end) {
    return start != null && end != null ? ticket.createdDate.between(start, end) : null;
  }

  private BooleanExpression startDateGoe(LocalDateTime value) {
    return value != null ? ticket.startDate.goe(value) : null;
  }

  private BooleanExpression startDateLoe(LocalDateTime value) {
    return value != null ? ticket.startDate.loe(value) : null;
  }

  private BooleanExpression usedDateGoe(LocalDateTime value) {
    return value != null ? ticket.usedDate.goe(value) : null;
  }

  private BooleanExpression usedDateLoe(LocalDateTime value) {
    return value != null ? ticket.usedDate.loe(value) : null;
  }

  private BooleanExpression kindIn(List<TicketKindType> value) {
    return value != null && value.size() > 0 ? ticket.kind.in(value) : null;
  }

  private BooleanExpression typeEq(TicketType value) {
    return value != null ? ticket.type.eq(value) : null;
  }

  private BooleanExpression stateIn(List<TicketStateType> value) {
    return value != null && value.size() > 0 ? ticket.state.in(value) : null;
  }

  private BooleanExpression ticketToday() {
    return Expressions.numberTemplate(
            Integer.class,
            "DATEDIFF({0}, {1})",
            DateTimeExpression.currentTimestamp(LocalDateTime.class),
            ticket.startDate)
        .eq(0)
        .or(ticket.type.eq(TicketType.INVITE));
  }

  private BooleanExpression createdDateYearEq(Integer value) {
    return value != null
        ? Expressions.numberTemplate(Integer.class, "YEAR({0})", ticket.createdDate).eq(value)
        : null;
  }
}
