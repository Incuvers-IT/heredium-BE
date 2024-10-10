package art.heredium.domain.post.repository;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import art.heredium.domain.post.entity.QPost;
import art.heredium.domain.post.model.dto.request.GetAdminPostRequest;
import art.heredium.domain.post.model.dto.response.PostResponse;

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryQueryDsl {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<PostResponse> search(GetAdminPostRequest dto, Pageable pageable) {
    QPost post = QPost.post;

    BooleanExpression whereClause = createWhereClause(dto);

    JPAQuery<PostResponse> query =
        queryFactory
            .select(
                Projections.constructor(
                    PostResponse.class,
                    post.id,
                    post.name,
                    post.imageUrl,
                    post.isEnabled,
                    post.contentDetail,
                    post.navigationLink,
                    post.admin.adminInfo.name,
                    post.createdDate,
                    post.thumbnailUrls))
            .from(post)
            .leftJoin(post.admin)
            .leftJoin(post.admin.adminInfo)
            .where(whereClause)
            .orderBy(post.createdDate.desc(), post.lastModifiedDate.desc());

    Long total = queryFactory.select(post.count()).from(post).where(whereClause).fetchOne();

    List<PostResponse> content =
        query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

    return new PageImpl<>(content, pageable, total != null ? total : 0L);
  }

  private BooleanExpression createWhereClause(GetAdminPostRequest dto) {
    return dateFilter(dto)
        .and(isEnabledEq(dto.getIsEnabled()))
        .and(nameContains(dto.getName()))
        .and(createdNameContains(dto.getCreatedName()));
  }

  private BooleanExpression dateFilter(GetAdminPostRequest dto) {
    if (dto.getSearchDateType() == GetAdminPostRequest.SearchDateType.CREATED_DATE) {
      return dateRangeFilter(QPost.post.createdDate, dto.getStartDate(), dto.getEndDate());
    } else if (dto.getSearchDateType() == GetAdminPostRequest.SearchDateType.LAST_MODIFIED_DATE) {
      return dateRangeFilter(QPost.post.lastModifiedDate, dto.getStartDate(), dto.getEndDate());
    }
    return null;
  }

  private BooleanExpression dateRangeFilter(
      DateTimePath<LocalDateTime> datePath, LocalDateTime startDate, LocalDateTime endDate) {
    BooleanExpression expression = null;
    if (startDate != null) {
      expression = datePath.goe(startDate);
    }
    if (endDate != null) {
      expression =
          expression != null ? expression.and(datePath.loe(endDate)) : datePath.loe(endDate);
    }
    return expression;
  }

  private BooleanExpression isEnabledEq(Boolean isEnabled) {
    return isEnabled != null ? QPost.post.isEnabled.eq(isEnabled) : null;
  }

  private BooleanExpression nameContains(String name) {
    return StringUtils.hasText(name) ? QPost.post.name.containsIgnoreCase(name) : null;
  }

  private BooleanExpression createdNameContains(String createdName) {
    return StringUtils.hasText(createdName)
        ? QPost.post.admin.adminInfo.name.containsIgnoreCase(createdName)
        : null;
  }
}
