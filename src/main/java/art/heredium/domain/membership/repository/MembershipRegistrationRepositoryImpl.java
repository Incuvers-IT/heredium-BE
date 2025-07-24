package art.heredium.domain.membership.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import art.heredium.domain.membership.entity.*;
import art.heredium.domain.membership.model.dto.response.ActiveMembershipDetailResponse;
import com.querydsl.core.types.ExpressionUtils;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import art.heredium.domain.account.entity.QAccount;
import art.heredium.domain.account.entity.QAccountInfo;
import art.heredium.domain.company.entity.QCompany;
import art.heredium.domain.coupon.entity.CouponType;
import art.heredium.domain.coupon.entity.QCoupon;
import art.heredium.domain.coupon.entity.QCouponUsage;
import art.heredium.domain.membership.model.dto.request.GetAllActiveMembershipsRequest;
import art.heredium.domain.membership.model.dto.response.ActiveMembershipRegistrationsResponse;

@RequiredArgsConstructor
public class MembershipRegistrationRepositoryImpl
    implements MembershipRegistrationRepositoryQueryDsl {
  private final JPAQueryFactory queryFactory;

  @Override
  public Page<ActiveMembershipRegistrationsResponse> getAllActiveMembershipRegistrations(
      GetAllActiveMembershipsRequest request, Pageable pageable) {

    QMembershipRegistration reg = QMembershipRegistration.membershipRegistration;
    QMembership            mem = QMembership.membership;
    QAccount               acct = QAccount.account;
    QAccountInfo           info = QAccountInfo.accountInfo;

    // (1) total count 쿼리
    long total = Optional.ofNullable(
            queryFactory
                    .select(acct.count())
                    .from(acct)
                    .innerJoin(acct.accountInfo, info)
                    .leftJoin(reg).on(reg.account.eq(acct))
                    .leftJoin(reg.membership, mem)
                    .where(
                            signedUpDateBetween(request.getSignUpDateFrom(), request.getSignUpDateTo()),
                            isAgreeToReceiveMarketing(request.getIsAgreeToReceiveMarketing()),
                            textContains(request.getText()),
                            paymentStatusIn(PaymentStatus.COMPLETED)
                    )
                    .fetchOne()
    ).orElse(0L);

    // (2) content 조회
    List<ActiveMembershipRegistrationsResponse> content = Collections.emptyList();
    if (total > 0) {
      content = queryActiveMembershipRegistrations(request)
              .offset(pageable.getOffset())
              .limit(pageable.getPageSize())
              .fetch();
    }

    return new PageImpl<>(content, pageable, total);
  }

  @Override
  public Page<ActiveMembershipDetailResponse> getActiveMembershipRegistrations(
          GetAllActiveMembershipsRequest request, Pageable pageable) {

    QMembershipRegistration membershipRegistration = QMembershipRegistration.membershipRegistration;
    QMembership membership = QMembership.membership;
    QAccount account = QAccount.account;
    QAccountInfo accountInfo = QAccountInfo.accountInfo;

    JPAQuery<ActiveMembershipDetailResponse> query =
            this.queryActiveMembershipRegistrationsDetail(request);

    // Create a count query
    JPAQuery<Long> countQuery =
            queryFactory
                    .select(account.count())
                    .from(account)
                    .innerJoin(account.accountInfo, accountInfo)
                    .leftJoin(membershipRegistration)
                    .on(membershipRegistration.account.id.eq(account.id))
                    .leftJoin(membership)
                    .on(membershipRegistration.membership.id.eq(membership.id))
                    .where(
                            signedUpDateBetween(request.getSignUpDateFrom(), request.getSignUpDateTo()),
                            isAgreeToReceiveMarketing(request.getIsAgreeToReceiveMarketing()),
                            textContains(request.getText()),
                            paymentStatusIn(PaymentStatus.COMPLETED),
                            account.id.eq(request.getAccountId())
                    );
    final long total = Optional.ofNullable(countQuery.fetchOne()).orElse(0L);
    List<ActiveMembershipDetailResponse> content = new ArrayList<>();
    if (total != 0) {
      content = query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();
    }
    return new PageImpl<>(content, pageable, total);
  }

  private BooleanExpression paymentStatusIn(PaymentStatus... paymentStatuses) {
    QMembershipRegistration membershipRegistration = QMembershipRegistration.membershipRegistration;
    if (paymentStatuses != null && paymentStatuses.length != 0) {
      return membershipRegistration.paymentStatus.in(paymentStatuses);
    }
    return null;
  }

  @Override
  public List<ActiveMembershipRegistrationsResponse> listActiveMembershipRegistrations(
      GetAllActiveMembershipsRequest request) {
    return this.queryActiveMembershipRegistrations(request).fetch();
  }

  private JPAQuery<ActiveMembershipRegistrationsResponse> queryActiveMembershipRegistrations(
          final GetAllActiveMembershipsRequest request) {

    QMembershipRegistration reg   = QMembershipRegistration.membershipRegistration;
    QMembership            mem   = QMembership.membership;
    QCompany               comp  = QCompany.company;
    QAccount               acct  = QAccount.account;
    QAccountInfo           info  = QAccountInfo.accountInfo;
    QMembershipMileage     mm    = QMembershipMileage.membershipMileage;

    return queryFactory
            .select(Projections.constructor(
                    ActiveMembershipRegistrationsResponse.class,
                    // 1) 가입 타입별 이름 분기
                    Expressions.cases()
                            .when(reg.registrationType.eq(RegistrationType.MEMBERSHIP_PACKAGE)).then(mem.name)
                            .when(reg.registrationType.eq(RegistrationType.COMPANY))          .then(comp.name)
                            .otherwise((String) null),
                    // 2) Account · Info
                    acct.id,
                    acct.email,
                    info.name,
                    info.phone,
                    // 3) 결제 상태 · 가입일
                    reg.paymentStatus,
                    reg.registrationDate,
                    // 4) 가입 회수
                    JPAExpressions.select(reg.count())
                            .from(reg)
                            .where(reg.account.eq(acct)),
                    // 5) 사용 쿠폰 건수 (EXHIBITION / PROGRAM / COFFEE)
                    countUsedCouponsByType(CouponType.EXHIBITION),
                    countUsedCouponsByType(CouponType.PROGRAM),
                    countUsedCouponsByType(CouponType.COFFEE),
                    // 6) 마케팅 수신 동의
                    info.isMarketingReceive,
                    // 7) **마일리지 합계**: 적립(+)/소멸(-) 전부 합산, null→0
                    ExpressionUtils.as(
                      JPAExpressions
                              .select(mm.mileageAmount.sum().coalesce(0))
                              .from(mm)
                              .where(mm.account.id.eq(acct.id)),
                      "mileageSum"
                    ))
            )
            .from(acct)
            .innerJoin(acct.accountInfo, info)
            .leftJoin(reg).on(reg.account.eq(acct))
            .leftJoin(reg.membership, mem)
            .leftJoin(reg.company, comp)
            .where(
                    signedUpDateBetween(request.getSignUpDateFrom(), request.getSignUpDateTo()),
                    isAgreeToReceiveMarketing(request.getIsAgreeToReceiveMarketing()),
                    textContains(request.getText()),
                    paymentStatusIn(PaymentStatus.COMPLETED)
            )
            .orderBy(reg.registrationDate.desc());
  }

  private JPAQuery<ActiveMembershipDetailResponse> queryActiveMembershipRegistrationsDetail(
          final GetAllActiveMembershipsRequest request) {
    QMembership membership = QMembership.membership;
    QCompany company = QCompany.company;
    QAccount account = QAccount.account;
    QAccountInfo accountInfo = QAccountInfo.accountInfo;
    QMembershipRegistration membershipRegistration = QMembershipRegistration.membershipRegistration;
    return queryFactory
            .select(
                    Projections.constructor(
                            ActiveMembershipDetailResponse.class,
                            Expressions.cases()
                                    .when(
                                            membershipRegistration.registrationType.eq(
                                                    RegistrationType.MEMBERSHIP_PACKAGE))
                                    .then(membership.name)
                                    .when(membershipRegistration.registrationType.eq(RegistrationType.COMPANY))
                                    .then(company.name)
                                    .otherwise((String) null),
                            account.id,
                            account.email,
                            account.createdDate,
                            accountInfo.name,
                            accountInfo.phone,
                            accountInfo.lastLoginDate,
                            accountInfo.gender,
                            accountInfo.birthDate,
                            accountInfo.isMarketingReceive,
                            membershipRegistration.paymentStatus,
                            membershipRegistration.registrationDate,
                            JPAExpressions.select(membershipRegistration.count())
                                    .from(membershipRegistration)
                                    .where(membershipRegistration.account.eq(account)),
                            countUsedCouponsByType(CouponType.EXHIBITION),
                            countUsedCouponsByType(CouponType.PROGRAM),
                            countUsedCouponsByType(CouponType.COFFEE),
                            accountInfo.isMarketingReceive))
            .from(account)
            .innerJoin(account.accountInfo, accountInfo)
            .leftJoin(membershipRegistration)
            .on(membershipRegistration.account.eq(account))
            .leftJoin(membershipRegistration.membership, membership)
            .leftJoin(membershipRegistration.company, company)
            .where(
                    signedUpDateBetween(request.getSignUpDateFrom(), request.getSignUpDateTo()),
                    isAgreeToReceiveMarketing(request.getIsAgreeToReceiveMarketing()),
                    textContains(request.getText()),
                    paymentStatusIn(PaymentStatus.COMPLETED),
                    account.id.eq(request.getAccountId())
            )
            .orderBy(membershipRegistration.registrationDate.desc());
  }

  private JPQLQuery<Long> countUsedCouponsByType(final CouponType couponType) {
    QCoupon coupon = QCoupon.coupon;
    QCouponUsage couponUsage = QCouponUsage.couponUsage;
    QAccount account = QAccount.account;
    return JPAExpressions.select(couponUsage.count())
        .from(couponUsage)
        .innerJoin(couponUsage.coupon, coupon)
        .where(
            couponUsage
                .account
                .eq(account)
                .and(couponUsage.isUsed.isTrue())
                .and(coupon.couponType.eq(couponType)));
  }

  // 해신: 만료일 체크 로직
  private BooleanExpression isNotExpired() {
    QMembershipRegistration membershipRegistration = QMembershipRegistration.membershipRegistration;
    DateTimeExpression<LocalDateTime> now =
        DateTimeExpression.currentTimestamp(LocalDateTime.class);
    return now.before(membershipRegistration.expirationDate);
  }

  private BooleanExpression isAgreeToReceiveMarketing(Boolean isAgreeToReceiveMarketing) {
    QAccountInfo accountInfo = QAccountInfo.accountInfo;
    if (isAgreeToReceiveMarketing == null) {
      return null;
    }
    return accountInfo.isMarketingReceive.eq(isAgreeToReceiveMarketing);
  }

  private BooleanExpression signedUpDateBetween(LocalDateTime from, LocalDateTime to) {
    QMembershipRegistration membershipRegistration = QMembershipRegistration.membershipRegistration;
    if (from != null && to != null) {
      return membershipRegistration.registrationDate.between(from, to);
    } else if (from != null) {
      return membershipRegistration.registrationDate.goe(from);
    } else if (to != null) {
      return membershipRegistration.registrationDate.loe(to);
    }
    return null;
  }

  private BooleanExpression textContains(String text) {
    QAccountInfo accountInfo = QAccountInfo.accountInfo;
    QMembership membership = QMembership.membership;
    QAccount account = QAccount.account;
    if (text != null) {
      return accountInfo
          .name
          .contains(text)
          .or(membership.name.contains(text))
          .or(accountInfo.phone.contains(text))
          .or(account.email.contains(text));
    }
    return null;
  }
}
