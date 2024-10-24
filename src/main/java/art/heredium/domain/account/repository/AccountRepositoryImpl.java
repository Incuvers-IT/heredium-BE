package art.heredium.domain.account.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.tika.utils.StringUtils;

import art.heredium.domain.account.entity.QAccount;
import art.heredium.domain.account.entity.QAccountInfo;
import art.heredium.domain.account.entity.QSleeperInfo;
import art.heredium.domain.account.model.dto.AccountMembershipRegistrationInfo;
import art.heredium.domain.account.model.dto.request.*;
import art.heredium.domain.account.model.dto.request.GetAccountWithMembershipInfoRequest;
import art.heredium.domain.account.model.dto.response.*;
import art.heredium.domain.account.model.dto.response.AccountWithMembershipInfoResponse;
import art.heredium.domain.coupon.entity.QCouponUsage;
import art.heredium.domain.membership.entity.PaymentStatus;
import art.heredium.domain.membership.entity.QMembership;
import art.heredium.domain.membership.entity.QMembershipRegistration;
import art.heredium.domain.ticket.entity.QTicket;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.domain.ticket.type.TicketType;

@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepositoryQueryDsl {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<GetAccountTicketGroupResponse> search(
      GetAccountTicketGroupRequest dto, Pageable pageable) {
    Long total =
        queryFactory
            .select(Wildcard.count)
            .from(QAccount.account)
            .innerJoin(QAccount.account.accountInfo, QAccountInfo.accountInfo)
            .where(searchFilter(dto))
            .fetch()
            .get(0);

    NumberPath<Long> visitCount = Expressions.numberPath(Long.class, "visitCount");
    List<GetAccountTicketGroupResponse> result =
        total > 0
            ? queryFactory
                .select(
                    new QGetAccountTicketGroupResponse(
                        QAccount.account.id,
                        QAccount.account.email,
                        QAccountInfo.accountInfo.name,
                        QAccountInfo.accountInfo.phone,
                        QAccount.account.createdDate,
                        QAccountInfo.accountInfo.lastLoginDate,
                        ExpressionUtils.as(selectVisitCount(), visitCount)))
                .from(QAccount.account)
                .innerJoin(QAccount.account.accountInfo, QAccountInfo.accountInfo)
                .where(searchFilter(dto))
                .orderBy(QAccount.account.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
            : new ArrayList<>();

    return new PageImpl<>(result, pageable, total);
  }

  @Override
  public Page<AccountWithMembershipInfoIncludingTitleResponse>
      searchWithMembershipInfoIncludingTitle(
          final GetAccountWithMembershipInfoIncludingTitleRequest dto, final Pageable pageable) {
    QAccount account = QAccount.account;
    QAccountInfo accountInfo = QAccountInfo.accountInfo;
    QCouponUsage couponUsage = QCouponUsage.couponUsage;
    QMembershipRegistration membershipRegistration = QMembershipRegistration.membershipRegistration;
    QMembership membership = QMembership.membership;

    JPAQuery<AccountWithMembershipInfoIncludingTitleResponse> query =
        this.listWithMembershipInfo(dto);

    // Create a count query
    JPAQuery<Long> countQuery =
        queryFactory
            .select(account.count())
            .from(account)
            .innerJoin(account.accountInfo, accountInfo)
            .leftJoin(membershipRegistration)
            .on(membershipRegistration.account.eq(account))
            .leftJoin(membershipRegistration.membership, membership)
            .where(
                paymentDateBetween(dto.getPaymentDateFrom(), dto.getPaymentDateTo()),
                paymentStatusIn(dto.getPaymentStatus()),
                textContains(dto.getText()));

    long total = countQuery.fetchOne();

    List<AccountWithMembershipInfoIncludingTitleResponse> content =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    return new PageImpl<>(content, pageable, total);
  }

  @Override
  public List<AccountWithMembershipInfoIncludingTitleResponse> listWithMembershipInfoIncludingTitle(
      final GetAccountWithMembershipInfoIncludingTitleRequest dto) {
    return this.listWithMembershipInfo(dto).fetch();
  }

  private JPAQuery<AccountWithMembershipInfoIncludingTitleResponse> listWithMembershipInfo(
      final GetAccountWithMembershipInfoIncludingTitleRequest dto) {
    QAccount account = QAccount.account;
    QAccountInfo accountInfo = QAccountInfo.accountInfo;
    QCouponUsage couponUsage = QCouponUsage.couponUsage;
    QMembershipRegistration membershipRegistration = QMembershipRegistration.membershipRegistration;
    QMembership membership = QMembership.membership;
    QTicket ticket = QTicket.ticket;

    return queryFactory
        .select(
            Projections.constructor(
                AccountWithMembershipInfoIncludingTitleResponse.class,
                membership.name,
                membershipRegistration.title,
                membershipRegistration.paymentStatus,
                membershipRegistration.paymentDate,
                membershipRegistration.registrationDate,
                membershipRegistration.expirationDate,
                JPAExpressions.select(couponUsage.count())
                    .from(couponUsage)
                    .where(couponUsage.account.eq(account).and(couponUsage.isUsed.isTrue())),
                account.email,
                accountInfo.name,
                accountInfo.phone,
                JPAExpressions.select(ticket.price.sum())
                    .from(ticket)
                    .where(ticket.account.eq(account))))
        .from(account)
        .innerJoin(account.accountInfo, accountInfo)
        .leftJoin(membershipRegistration)
        .on(membershipRegistration.account.eq(account))
        .leftJoin(membershipRegistration.membership, membership)
        .where(
            paymentDateBetween(dto.getPaymentDateFrom(), dto.getPaymentDateTo()),
            paymentStatusIn(dto.getPaymentStatus()),
            textContains(dto.getText()));
  }

  private JPQLQuery<Long> selectVisitCount() {
    return JPAExpressions.select(Wildcard.count)
        .from(QTicket.ticket)
        .where(
            QTicket.ticket.account.id.eq(QAccount.account.id),
            QTicket.ticket.kind.eq(TicketKindType.EXHIBITION),
            QTicket.ticket.type.ne(TicketType.INVITE),
            QTicket.ticket.state.eq(TicketStateType.USED));
  }

  @Override
  public Page<GetAccountTicketInviteResponse> search(
      GetAccountTicketInviteRequest dto, Pageable pageable) {
    NumberPath<Long> visitCount = Expressions.numberPath(Long.class, "visitCount");
    NumberPath<Long> inviteCount = Expressions.numberPath(Long.class, "inviteCount");

    JPQLQuery<Long> selectVisitCount = selectVisitCount();
    JPQLQuery<Long> selectInviteCount =
        JPAExpressions.select(Wildcard.count)
            .from(QTicket.ticket)
            .where(
                QTicket.ticket.account.id.eq(QAccount.account.id),
                QTicket.ticket.kind.eq(TicketKindType.EXHIBITION),
                QTicket.ticket.type.eq(TicketType.INVITE));

    Long total =
        queryFactory
            .select(Wildcard.count)
            .from(QAccount.account)
            .innerJoin(QAccount.account.accountInfo, QAccountInfo.accountInfo)
            .where(searchFilter(dto, selectVisitCount, selectInviteCount))
            .fetch()
            .get(0);
    List<GetAccountTicketInviteResponse> result =
        total > 0
            ? queryFactory
                .select(
                    new QGetAccountTicketInviteResponse(
                        QAccount.account.id,
                        QAccount.account.email,
                        QAccountInfo.accountInfo.name,
                        QAccountInfo.accountInfo.phone,
                        QAccount.account.createdDate,
                        QAccountInfo.accountInfo.lastLoginDate,
                        QAccountInfo.accountInfo.isLocalResident,
                        ExpressionUtils.as(selectVisitCount, visitCount),
                        ExpressionUtils.as(selectInviteCount, inviteCount)))
                .from(QAccount.account)
                .innerJoin(QAccount.account.accountInfo, QAccountInfo.accountInfo)
                .where(searchFilter(dto, selectVisitCount, selectInviteCount))
                .orderBy(QAccount.account.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
            : new ArrayList<>();

    return new PageImpl<>(result, pageable, total);
  }

  @Override
  public Page<GetAdminAccountResponse> search(GetAdminAccountRequest dto, Pageable pageable) {
    Long total =
        queryFactory
            .select(Wildcard.count)
            .from(QAccount.account)
            .innerJoin(QAccount.account.accountInfo, QAccountInfo.accountInfo)
            .where(searchFilter(dto))
            .fetch()
            .get(0);

    List<GetAdminAccountResponse> result =
        total > 0
            ? getGetAdminAccountQuery(dto)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
            : new ArrayList<>();

    return new PageImpl<>(result, pageable, total);
  }

  @Override
  public List<GetAdminAccountResponse> search(GetAdminAccountRequest dto) {
    return getGetAdminAccountQuery(dto).fetch();
  }

  private JPAQuery<GetAdminAccountResponse> getGetAdminAccountQuery(GetAdminAccountRequest dto) {
    NumberPath<Long> visitCount = Expressions.numberPath(Long.class, "visitCount");
    return queryFactory
        .select(
            new QGetAdminAccountResponse(
                QAccount.account.id,
                QAccount.account.email,
                QAccountInfo.accountInfo.name,
                QAccountInfo.accountInfo.phone,
                QAccount.account.createdDate,
                QAccountInfo.accountInfo.lastLoginDate,
                QAccountInfo.accountInfo.isMarketingReceive,
                ExpressionUtils.as(selectVisitCount(), visitCount)))
        .from(QAccount.account)
        .innerJoin(QAccount.account.accountInfo, QAccountInfo.accountInfo)
        .where(searchFilter(dto))
        .orderBy(QAccount.account.createdDate.desc());
  }

  @Override
  public Page<GetAdminSleeperResponse> search(GetAdminSleeperRequest dto, Pageable pageable) {
    Long total =
        queryFactory
            .select(Wildcard.count)
            .from(QAccount.account)
            .innerJoin(QSleeperInfo.sleeperInfo)
            .on(QAccount.account.id.eq(QSleeperInfo.sleeperInfo.id))
            .where(searchFilter(dto))
            .fetch()
            .get(0);

    List<GetAdminSleeperResponse> result =
        total > 0
            ? getGetAdminAccountQuery(dto)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
            : new ArrayList<>();

    return new PageImpl<>(result, pageable, total);
  }

  @Override
  public List<GetAdminSleeperResponse> search(GetAdminSleeperRequest dto) {
    return getGetAdminAccountQuery(dto).fetch();
  }

  private JPAQuery<GetAdminSleeperResponse> getGetAdminAccountQuery(GetAdminSleeperRequest dto) {
    NumberPath<Long> visitCount = Expressions.numberPath(Long.class, "visitCount");
    return queryFactory
        .select(
            new QGetAdminSleeperResponse(
                QAccount.account.id,
                QAccount.account.email,
                QSleeperInfo.sleeperInfo.name,
                QSleeperInfo.sleeperInfo.phone,
                QAccount.account.createdDate,
                QSleeperInfo.sleeperInfo.sleepDate,
                QSleeperInfo.sleeperInfo.isMarketingReceive,
                ExpressionUtils.as(selectVisitCount(), visitCount)))
        .from(QAccount.account)
        .innerJoin(QSleeperInfo.sleeperInfo)
        .on(QAccount.account.id.eq(QSleeperInfo.sleeperInfo.id))
        .where(searchFilter(dto))
        .orderBy(QSleeperInfo.sleeperInfo.sleepDate.desc());
  }

  private BooleanBuilder searchFilter(GetAccountTicketGroupRequest dto) {
    BooleanBuilder builder = new BooleanBuilder();
    if (!StringUtils.isBlank(dto.getText())) {
      BooleanBuilder textBuilder = new BooleanBuilder();
      textBuilder.or(emailContain(dto.getText()));
      textBuilder.or(nameContain(dto.getText()));
      builder.and(textBuilder);
    }
    return builder;
  }

  private BooleanBuilder searchFilter(GetAdminAccountRequest dto) {
    BooleanBuilder builder = new BooleanBuilder();
    if (dto.getSearchDateType().equals(GetAdminAccountRequest.SearchDateType.CREATED_DATE)) {
      builder.and(createdDateGoe(dto.getStartDate()));
      builder.and(createdDateLoe(dto.getEndDate()));
    } else if (dto.getSearchDateType()
        .equals(GetAdminAccountRequest.SearchDateType.LAST_LOGIN_DATE)) {
      builder.and(lastLoginDateGoe(dto.getStartDate()));
      builder.and(lastLoginDateLoe(dto.getEndDate()));
    }

    builder.and(isMarketingReceiveEq(dto.getIsMarketingReceive()));

    if (!StringUtils.isBlank(dto.getText())) {
      BooleanBuilder textBuilder = new BooleanBuilder();
      textBuilder.or(emailContain(dto.getText()));
      textBuilder.or(nameContain(dto.getText()));
      textBuilder.or(phoneContain(dto.getText()));
      builder.and(textBuilder);
    }
    return builder;
  }

  private BooleanBuilder searchFilter(GetAdminSleeperRequest dto) {
    BooleanBuilder builder = new BooleanBuilder();
    if (dto.getSearchDateType().equals(GetAdminSleeperRequest.SearchDateType.CREATED_DATE)) {
      builder.and(createdDateGoe(dto.getStartDate()));
      builder.and(createdDateLoe(dto.getEndDate()));
    } else if (dto.getSearchDateType().equals(GetAdminSleeperRequest.SearchDateType.SLEEP_DATE)) {
      builder.and(sleepDateGoe(dto.getStartDate()));
      builder.and(sleepDateLoe(dto.getEndDate()));
    }

    builder.and(sleeperIsMarketingReceiveEq(dto.getIsMarketingReceive()));

    if (!StringUtils.isBlank(dto.getText())) {
      BooleanBuilder textBuilder = new BooleanBuilder();
      textBuilder.or(emailContain(dto.getText()));
      textBuilder.or(sleeperNameContain(dto.getText()));
      textBuilder.or(sleeperPhoneContain(dto.getText()));
      builder.and(textBuilder);
    }
    return builder;
  }

  private BooleanBuilder searchFilter(
      GetAccountTicketInviteRequest dto, JPQLQuery<Long> visitCount, JPQLQuery<Long> inviteCount) {
    BooleanBuilder builder = new BooleanBuilder();
    if (dto.getSearchDateType().equals(GetAccountTicketInviteRequest.SearchDateType.CREATED_DATE)) {
      builder.and(createdDateGoe(dto.getStartDate()));
      builder.and(createdDateLoe(dto.getEndDate()));
    } else if (dto.getSearchDateType()
        .equals(GetAccountTicketInviteRequest.SearchDateType.LAST_LOGIN_DATE)) {
      builder.and(lastLoginDateGoe(dto.getStartDate()));
      builder.and(lastLoginDateLoe(dto.getEndDate()));
    }
    builder.and(localResident(dto.getIsLocalResident()));

    if (!StringUtils.isBlank(dto.getText())) {
      BooleanBuilder textBuilder = new BooleanBuilder();
      textBuilder.or(emailContain(dto.getText()));
      textBuilder.or(nameContain(dto.getText()));
      textBuilder.or(phoneContain(dto.getText()));
      builder.and(textBuilder);
    }

    builder.and(visitCount.goe(dto.getMinVisit()).and(visitCount.loe(dto.getMaxVisit())));
    builder.and(inviteCount.goe(dto.getMinInvite()).and(inviteCount.loe(dto.getMaxInvite())));
    builder.and(idNotIn(dto.getExcludeIds()));

    return builder;
  }

  private BooleanExpression createdDateGoe(LocalDateTime value) {
    return value != null ? QAccount.account.createdDate.goe(value) : null;
  }

  private BooleanExpression createdDateLoe(LocalDateTime value) {
    return value != null ? QAccount.account.createdDate.loe(value) : null;
  }

  private BooleanExpression lastLoginDateGoe(LocalDateTime value) {
    return value != null ? QAccountInfo.accountInfo.lastLoginDate.goe(value) : null;
  }

  private BooleanExpression lastLoginDateLoe(LocalDateTime value) {
    return value != null ? QAccountInfo.accountInfo.lastLoginDate.loe(value) : null;
  }

  private BooleanExpression sleepDateGoe(LocalDateTime value) {
    return value != null ? QSleeperInfo.sleeperInfo.sleepDate.goe(value) : null;
  }

  private BooleanExpression sleepDateLoe(LocalDateTime value) {
    return value != null ? QSleeperInfo.sleeperInfo.sleepDate.loe(value) : null;
  }

  private BooleanExpression localResident(Boolean value) {
    return value != null ? QAccountInfo.accountInfo.isLocalResident.eq(value) : null;
  }

  private BooleanExpression emailContain(String value) {
    return value != null ? QAccount.account.email.contains(value) : null;
  }

  private BooleanExpression nameContain(String value) {
    return value != null ? QAccountInfo.accountInfo.name.contains(value) : null;
  }

  private BooleanExpression phoneContain(String value) {
    return value != null ? QAccountInfo.accountInfo.phone.contains(value) : null;
  }

  private Predicate isMarketingReceiveEq(Boolean value) {
    return value != null ? QAccountInfo.accountInfo.isMarketingReceive.eq(value) : null;
  }

  private Predicate sleeperIsMarketingReceiveEq(Boolean value) {
    return value != null ? QSleeperInfo.sleeperInfo.isMarketingReceive.eq(value) : null;
  }

  private BooleanExpression sleeperNameContain(String value) {
    return value != null ? QSleeperInfo.sleeperInfo.name.contains(value) : null;
  }

  private BooleanExpression sleeperPhoneContain(String value) {
    return value != null ? QSleeperInfo.sleeperInfo.phone.contains(value) : null;
  }

  private BooleanExpression idNotIn(List<Long> value) {
    return value != null && value.size() > 0 ? QAccount.account.id.notIn(value) : null;
  }

  @Override
  public Page<AccountWithMembershipInfoResponse> searchWithMembershipInfo(
      GetAccountWithMembershipInfoRequest dto, Pageable pageable) {
    QAccount account = QAccount.account;
    QAccountInfo accountInfo = QAccountInfo.accountInfo;
    QTicket ticket = QTicket.ticket;
    QCouponUsage couponUsage = QCouponUsage.couponUsage;
    QMembershipRegistration membershipRegistration = QMembershipRegistration.membershipRegistration;
    QMembership membership = QMembership.membership;

    JPAQuery<AccountWithMembershipInfoResponse> query =
        queryFactory
            .select(
                Projections.constructor(
                    AccountWithMembershipInfoResponse.class,
                    account.id,
                    account.email,
                    accountInfo.name,
                    accountInfo.phone,
                    account.createdDate,
                    accountInfo.lastLoginDate,
                    JPAExpressions.selectOne()
                        .from(ticket)
                        .where(
                            ticket
                                .account
                                .eq(account)
                                .and(
                                    ticket.kind.in(
                                        TicketKindType.PROGRAM, TicketKindType.EXHIBITION)))
                        .exists(),
                    JPAExpressions.selectOne()
                        .from(couponUsage)
                        .where(couponUsage.account.eq(account).and(couponUsage.isUsed.isTrue()))
                        .exists(),
                    membership.name,
                    JPAExpressions.select(Wildcard.count)
                        .from(ticket)
                        .where(
                            ticket.account.eq(account),
                            ticket.kind.in(TicketKindType.EXHIBITION, TicketKindType.PROGRAM),
                            ticket.state.eq(TicketStateType.USED)),
                    Projections.constructor(
                        AccountMembershipRegistrationInfo.class,
                        membershipRegistration.id,
                        membershipRegistration.registrationDate,
                        membershipRegistration.expirationDate)))
            .from(account)
            .innerJoin(account.accountInfo, accountInfo)
            .leftJoin(membershipRegistration)
            .on(membershipRegistration.account.eq(account))
            .leftJoin(membershipRegistration.membership, membership)
            .where(
                createdDateBetween(dto.getSignUpDateFrom(), dto.getSignUpDateTo()),
                hasNumberOfEntries(dto.getHasNumberOfEntries()),
                alreadyLoginedBefore(dto.getAlreadyLoginedBefore()),
                alreadyUsedCouponBefore(dto.getAlreadyUsedCouponBefore()),
                hasMembership(dto.getHasMembership()),
                searchByText(dto.getText()),
                idNotIn(dto.getExcludeIds()));

    // Create a count query
    JPAQuery<Long> countQuery =
        queryFactory
            .select(account.count())
            .from(account)
            .innerJoin(account.accountInfo, accountInfo)
            .leftJoin(membershipRegistration)
            .on(membershipRegistration.account.eq(account))
            .leftJoin(membershipRegistration.membership, membership)
            .where(
                createdDateBetween(dto.getSignUpDateFrom(), dto.getSignUpDateTo()),
                hasNumberOfEntries(dto.getHasNumberOfEntries()),
                alreadyLoginedBefore(dto.getAlreadyLoginedBefore()),
                alreadyUsedCouponBefore(dto.getAlreadyUsedCouponBefore()),
                hasMembership(dto.getHasMembership()),
                searchByText(dto.getText()),
                idNotIn(dto.getExcludeIds()));

    long total = countQuery.fetchOne();

    List<AccountWithMembershipInfoResponse> content =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    return new PageImpl<>(content, pageable, total);
  }

  private BooleanExpression createdDateBetween(LocalDateTime from, LocalDateTime to) {
    QAccount account = QAccount.account;
    if (from != null && to != null) {
      return account.createdDate.between(from, to);
    } else if (from != null) {
      return account.createdDate.goe(from);
    } else if (to != null) {
      return account.createdDate.loe(to);
    }
    return null;
  }

  private BooleanExpression paymentDateBetween(LocalDateTime from, LocalDateTime to) {
    QMembershipRegistration membershipRegistration = QMembershipRegistration.membershipRegistration;
    if (from != null && to != null) {
      return membershipRegistration.paymentDate.between(LocalDate.from(from), LocalDate.from(to));
    } else if (from != null) {
      return membershipRegistration.paymentDate.goe(LocalDate.from(from));
    } else if (to != null) {
      return membershipRegistration.paymentDate.loe(LocalDate.from(to));
    }
    return null;
  }

  private BooleanExpression textContains(String text) {
    QAccountInfo accountInfo = QAccountInfo.accountInfo;
    QAccount account = QAccount.account;
    if (text != null) {
      return accountInfo
          .name
          .contains(text)
          .or(accountInfo.phone.contains(text))
          .or(account.email.contains(text));
    }
    return null;
  }

  private BooleanExpression paymentStatusIn(List<PaymentStatus> paymentStatuses) {
    QMembershipRegistration membershipRegistration = QMembershipRegistration.membershipRegistration;
    if (paymentStatuses != null && !paymentStatuses.isEmpty()) {
      return membershipRegistration.paymentStatus.in(paymentStatuses);
    }
    return null;
  }

  private BooleanExpression hasNumberOfEntries(Boolean hasEntries) {
    if (Boolean.TRUE.equals(hasEntries)) {
      QAccount account = QAccount.account;
      QTicket ticket = QTicket.ticket;
      return JPAExpressions.selectOne()
          .from(ticket)
          .where(
              ticket
                  .account
                  .eq(account)
                  .and(ticket.kind.in(TicketKindType.PROGRAM, TicketKindType.EXHIBITION)),
              ticket.state.eq(TicketStateType.USED))
          .exists();
    }
    return null;
  }

  private BooleanExpression alreadyLoginedBefore(Boolean alreadyLogined) {
    if (Boolean.TRUE.equals(alreadyLogined)) {
      QAccountInfo accountInfo = QAccountInfo.accountInfo;
      return accountInfo.lastLoginDate.isNotNull();
    }
    return null;
  }

  private BooleanExpression alreadyUsedCouponBefore(Boolean alreadyUsedCoupon) {
    if (Boolean.TRUE.equals(alreadyUsedCoupon)) {
      QAccount account = QAccount.account;
      QCouponUsage couponUsage = QCouponUsage.couponUsage;
      return JPAExpressions.selectOne()
          .from(couponUsage)
          .where(couponUsage.account.eq(account).and(couponUsage.isUsed.isTrue()))
          .exists();
    }
    return null;
  }

  private BooleanExpression hasMembership(Boolean hasMembership) {
    if (Boolean.TRUE.equals(hasMembership)) {
      QAccount account = QAccount.account;
      QMembershipRegistration membershipRegistration =
          QMembershipRegistration.membershipRegistration;
      LocalDate currentDate = LocalDate.now();

      return JPAExpressions.selectOne()
          .from(membershipRegistration)
          .where(
              membershipRegistration
                  .account
                  .eq(account)
                  .and(membershipRegistration.expirationDate.goe(currentDate)))
          .exists();
    }
    return null;
  }

  private BooleanExpression membershipNameContain(String text) {
    if (StringUtils.isEmpty(text)) return null;
    QMembership membership = QMembership.membership;
    return JPAExpressions.selectOne()
        .from(membership)
        .where(membership.name.contains(text))
        .exists();
  }

  private BooleanBuilder searchByText(String text) {
    BooleanBuilder builder = new BooleanBuilder();
    if (StringUtils.isEmpty(text)) return builder;
    BooleanBuilder textBuilder = new BooleanBuilder();
    textBuilder.or(membershipNameContain(text));
    textBuilder.or(nameContain(text));
    textBuilder.or(emailContain(text));
    textBuilder.or(phoneContain(text));
    builder.and(textBuilder);
    return builder;
  }
}
