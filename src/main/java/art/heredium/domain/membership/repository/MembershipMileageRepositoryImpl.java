package art.heredium.domain.membership.repository;

import art.heredium.domain.account.entity.QAccount;
import art.heredium.domain.account.entity.QAccountInfo;
import art.heredium.domain.coffee.entity.QCoffee;
import art.heredium.domain.company.entity.QCompany;
import art.heredium.domain.exhibition.entity.QExhibition;
import art.heredium.domain.membership.entity.QMembership;
import art.heredium.domain.membership.entity.QMembershipMileage;
import art.heredium.domain.membership.entity.QMembershipRegistration;
import art.heredium.domain.membership.entity.RegistrationType;
import art.heredium.domain.membership.model.dto.request.GetAllActiveMembershipsRequest;
import art.heredium.domain.membership.model.dto.response.MembershipMileageResponse;
import art.heredium.domain.program.entity.QProgram;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    // 3) category별 subtitle 선택 식
    StringExpression titleExpr = new CaseBuilder()
            .when(mm.category.in(0, 3)).then(ex.title)
            .when(mm.category.eq(1)).then(pr.title)
            .when(mm.category.eq(2)).then(cf.title)
            .otherwise((String) null);

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
                    titleExpr
            ))
            .from(mm)
            .leftJoin(ex)
            .on(mm.category.in(0, 3)
                    .and(mm.categoryId.eq(ex.id)))
            .leftJoin(pr)
            .on(mm.category.eq(1)
                    .and(mm.categoryId.eq(pr.id)))
            .leftJoin(cf)
            .on(mm.category.eq(2)
                    .and(mm.categoryId.eq(cf.id)))
            .where(mm.account.id.eq(request.getAccountId()))
            .orderBy(mm.createdDate.desc());

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
