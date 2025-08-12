package art.heredium.domain.membership.repository;

import art.heredium.domain.coffee.entity.QCoffee;
import art.heredium.domain.exhibition.entity.QExhibition;
import art.heredium.domain.membership.entity.QMembershipMileage;
import art.heredium.domain.membership.model.dto.request.GetAllActiveMembershipsRequest;
import art.heredium.domain.membership.model.dto.request.MembershipMileageSearchRequest;
import art.heredium.domain.membership.model.dto.response.MembershipMileageResponse;
import art.heredium.domain.program.entity.QProgram;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import static art.heredium.domain.ticket.type.TicketKindType.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class MembershipMileageRepositoryImpl
    implements MembershipMileageRepositoryQueryDsl {
  private final JPAQueryFactory queryFactory;

  @Override
  public Page<MembershipMileageResponse> getMembershipsMileageList(
      GetAllActiveMembershipsRequest request, Pageable pageable) {

    QMembershipMileage mm = QMembershipMileage.membershipMileage;
    QExhibition ex = QExhibition.exhibition;
    QProgram pr = QProgram.program;
    QCoffee cf = QCoffee.coffee;

    QMembershipMileage parent = new QMembershipMileage("parent");

    NumberExpression<Long> groupKeyExpr =
            new CaseBuilder()
                    .when(mm.relatedMileage.id.isNull()).then(mm.id)
                    .otherwise(mm.relatedMileage.id);

    // 제목 분기
    StringExpression titleExpr = new CaseBuilder()
            .when(mm.category.in(EXHIBITION, ARTSHOP)).then(ex.title)
            .when(mm.category.eq(PROGRAM)).then(pr.title)
            .when(mm.category.eq(COFFEE)).then(cf.title)
            .otherwise((String) null);

    // 부모(적립) 먼저 = 0, 자식(취소) = 1
    NumberExpression<Integer> childOrderExpr = new CaseBuilder()
            .when(mm.relatedMileage.id.isNull()).then(0)
            .otherwise(1);

    // 그룹 전체를 정렬할 기준: 부모의 createdDate
    DateTimeExpression<LocalDateTime> groupDateExpr = new CaseBuilder()
            .when(mm.relatedMileage.id.isNotNull()).then(parent.createdDate)
            .otherwise(mm.createdDate);

    BooleanBuilder where = new BooleanBuilder();
    where.and(mm.account.id.eq(request.getAccountId()));

    if (request.getType() != null && !request.getType().isEmpty()) {
      where.and(mm.type.in(request.getType()));
    }

    JPAQuery<MembershipMileageResponse> query = queryFactory
            .select(Projections.constructor(
                    MembershipMileageResponse.class,
                    mm.id,
                    mm.account.id,
                    mm.type,
                    mm.category,
                    mm.categoryId,
                    mm.paymentMethod,
                    mm.paymentAmount,
                    mm.serialNumber,
                    mm.mileageAmount,
                    mm.expirationDate,
                    mm.createdName,
                    mm.createdDate,
                    mm.lastModifiedName,
                    mm.lastModifiedDate,
                    titleExpr,
                    mm.remark,
                    mm.relatedMileage.id.as("relatedMileageId"),
                    mm.ticket.id.as("ticketId")
            ))
            .from(mm)
            // 제목 용 조인
            .leftJoin(ex).on(mm.category.in(EXHIBITION, ARTSHOP).and(mm.categoryId.eq(ex.id)))
            .leftJoin(pr).on(mm.category.eq(PROGRAM).and(mm.categoryId.eq(pr.id)))
            .leftJoin(cf).on(mm.category.eq(COFFEE).and(mm.categoryId.eq(cf.id)))
            // self‑join: 부모 엔티티
            .leftJoin(mm.relatedMileage, parent)
            .where(where)
//            .where(mm.account.id.eq(request.getAccountId()))
            .orderBy(
                    // 1) 그룹(부모) 생성일 역순
                    groupDateExpr.desc(),
                    // 2) 그룹 키(동일 그룹끼리 묶기)
                    groupKeyExpr.desc(),
                    // 3) 그룹 내 부모(0) → 자식(1)
                    childOrderExpr.asc(),
                    // 4) 동일 그룹 내 타이브레이커
                    mm.createdDate.desc(),
                    mm.id.desc()
            );

    // 카운트 쿼리
    long total = query.fetchCount();

    // 페이징
    List<MembershipMileageResponse> content = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    return new PageImpl<>(content, pageable, total);
  }

  @Override
  public Page<MembershipMileageResponse> getUserMembershipsMileageList(
          MembershipMileageSearchRequest request, Pageable pageable) {

    QMembershipMileage mm = QMembershipMileage.membershipMileage;
    BooleanBuilder where = new BooleanBuilder();
    where.and(mm.account.id.eq(request.getAccountId()));

    if (request.getTypes() != null && !request.getTypes().isEmpty()) {
      where.and(mm.type.in(request.getTypes()));
    }

    if (request.getStartDate() != null) {
      where.and(mm.createdDate.goe(request.getStartDate()));
    }
    if (request.getEndDate() != null) {
      where.and(mm.createdDate.loe(request.getEndDate()));
    }

    JPAQuery<MembershipMileageResponse> query = queryFactory
            .select(Projections.constructor(
                    MembershipMileageResponse.class,
                    mm.id,
                    mm.account.id,
                    mm.type,
                    mm.category,
                    mm.categoryId,
                    mm.paymentMethod,
                    mm.paymentAmount,
                    mm.serialNumber,
                    mm.mileageAmount,
                    mm.expirationDate,
                    mm.createdName,
                    mm.createdDate,
                    mm.lastModifiedName,
                    mm.lastModifiedDate,
                    Expressions.constant(""),
                    mm.remark,
                    mm.relatedMileage.id.as("relatedMileageId"),
                    mm.ticket.id.as("ticketId")
            ))
            .from(mm)
            .where(where)
            .orderBy(
                    // 3) 각 레코드 자신의 생성일 역순
                    mm.createdDate.desc()
            );

    // 카운트 쿼리
    long total = query.fetchCount();

    // 페이징
    List<MembershipMileageResponse> content = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    return new PageImpl<>(content, pageable, total);
  }
}
