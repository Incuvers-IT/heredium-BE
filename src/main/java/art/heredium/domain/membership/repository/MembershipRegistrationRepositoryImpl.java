package art.heredium.domain.membership.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import art.heredium.domain.account.entity.QAccount;
import art.heredium.domain.account.entity.QAccountInfo;
import art.heredium.domain.coupon.entity.CouponType;
import art.heredium.domain.coupon.entity.QCoupon;
import art.heredium.domain.coupon.entity.QCouponUsage;
import art.heredium.domain.membership.entity.QMembership;
import art.heredium.domain.membership.entity.QMembershipRegistration;
import art.heredium.domain.membership.model.dto.request.GetAllActiveMembershipsRequest;
import art.heredium.domain.membership.model.dto.response.ActiveMembershipRegistrationsResponse;

@RequiredArgsConstructor
public class MembershipRegistrationRepositoryImpl
    implements MembershipRegistrationRepositoryQueryDsl {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<ActiveMembershipRegistrationsResponse> getAllActiveMembershipRegistrations(
      GetAllActiveMembershipsRequest request, Pageable pageable) {
    QMembershipRegistration membershipRegistration = QMembershipRegistration.membershipRegistration;
    QMembership membership = QMembership.membership;
    QAccount account = QAccount.account;
    QCoupon coupon = QCoupon.coupon;
    QAccountInfo accountInfo = QAccountInfo.accountInfo;
    QCouponUsage couponUsage = QCouponUsage.couponUsage;
    JPAQuery<ActiveMembershipRegistrationsResponse> query =
        queryFactory
            .select(
                Projections.constructor(
                    ActiveMembershipRegistrationsResponse.class,
                    membership.name,
                    account.id,
                    accountInfo.name,
                    accountInfo.phone,
                    membershipRegistration.paymentStatus,
                    membershipRegistration.paymentDate,
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
            .leftJoin(membership)
            .on(membershipRegistration.membership.eq(membership))
            .where(
                signedUpDateBetween(request.getSignUpDateFrom(), request.getSignUpDateTo()),
                isAgreeToReceiveMarketing(request.getIsAgreeToReceiveMarketing()),
                textContains(request.getText()),
                isNotExpired());

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
                isNotExpired());
    final long total = countQuery.fetchOne();
    List<ActiveMembershipRegistrationsResponse> content =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();
    return new PageImpl<>(content, pageable, total);
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

  private BooleanExpression isNotExpired() {
    QMembershipRegistration membershipRegistration = QMembershipRegistration.membershipRegistration;
    DateTimeExpression<LocalDate> now = DateTimeExpression.currentTimestamp(LocalDate.class);
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
      return membershipRegistration.registrationDate.between(
          LocalDate.from(from), LocalDate.from(to));
    } else if (from != null) {
      return membershipRegistration.registrationDate.goe(LocalDate.from(from));
    } else if (to != null) {
      return membershipRegistration.registrationDate.loe(LocalDate.from(to));
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
