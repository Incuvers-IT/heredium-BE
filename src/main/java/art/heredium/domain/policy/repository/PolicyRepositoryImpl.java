package art.heredium.domain.policy.repository;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.tika.utils.StringUtils;

import art.heredium.domain.policy.entity.Policy;
import art.heredium.domain.policy.entity.QPolicy;
import art.heredium.domain.policy.model.dto.request.GetAdminPolicyRequest;
import art.heredium.domain.policy.type.PolicyType;

@RequiredArgsConstructor
public class PolicyRepositoryImpl implements PolicyRepositoryQueryDsl {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Policy> search(GetAdminPolicyRequest dto, Pageable pageable) {
    Long total =
        queryFactory
            .select(Wildcard.count)
            .from(QPolicy.policy)
            .where(searchFilter(dto))
            .fetch()
            .get(0);

    List<Policy> result =
        total > 0
            ? queryFactory
                .selectFrom(QPolicy.policy)
                .where(searchFilter(dto))
                .orderBy(QPolicy.policy.postDate.desc(), QPolicy.policy.lastModifiedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
            : new ArrayList<>();

    return new PageImpl<>(result, pageable, total);
  }

  private BooleanBuilder searchFilter(GetAdminPolicyRequest dto) {
    BooleanBuilder builder = new BooleanBuilder();

    if (!StringUtils.isBlank(dto.getText())) {
      BooleanBuilder textFilter = new BooleanBuilder();
      textFilter.or(titleContain(dto.getText()));
      textFilter.or(createdNameContain(dto.getText()));
      textFilter.or(lastModifiedNameContain(dto.getText()));
      builder.and(textFilter);
    }
    builder.and(typeEqual(dto.getType()));
    return builder;
  }

  private BooleanExpression typeEqual(PolicyType value) {
    return value != null ? QPolicy.policy.type.eq(value) : null;
  }

  private BooleanExpression titleContain(String value) {
    return !StringUtils.isBlank(value) ? QPolicy.policy.title.contains(value) : null;
  }

  private BooleanExpression createdNameContain(String value) {
    return !StringUtils.isBlank(value) ? QPolicy.policy.createdName.contains(value) : null;
  }

  private BooleanExpression lastModifiedNameContain(String value) {
    return !StringUtils.isBlank(value) ? QPolicy.policy.lastModifiedName.contains(value) : null;
  }
}
