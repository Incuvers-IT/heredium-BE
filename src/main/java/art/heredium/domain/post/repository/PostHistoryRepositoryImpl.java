package art.heredium.domain.post.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import art.heredium.domain.post.entity.QPostHistory;
import art.heredium.domain.post.model.dto.request.PostHistorySearchRequest;
import art.heredium.domain.post.model.dto.response.PostHistoryBaseResponse;

@RequiredArgsConstructor
public class PostHistoryRepositoryImpl implements PostHistoryRepositoryQueryDsl {
  private final JPAQueryFactory queryFactory;

  @Override
  public Page<PostHistoryBaseResponse> search(PostHistorySearchRequest request, Pageable pageable) {
    QPostHistory postHistory = QPostHistory.postHistory;
    JPAQuery<PostHistoryBaseResponse> query = this.queryPostHistory(request);

    // Create a count query
    JPAQuery<Long> countQuery =
        queryFactory
            .select(postHistory.count())
            .from(postHistory)
            .where(
                lastModifiedDateBetween(request.getModifyDateFrom(), request.getModifyDateTo()),
                modifyUserEmailOrName(request.getModifyUserEmailOrName()));

    final long total = Optional.ofNullable(countQuery.fetchOne()).orElse(0L);
    List<PostHistoryBaseResponse> content = new ArrayList<>();
    if (total != 0) {
      content = query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();
    }
    return new PageImpl<>(content, pageable, total);
  }

  private JPAQuery<PostHistoryBaseResponse> queryPostHistory(PostHistorySearchRequest request) {
    QPostHistory postHistory = QPostHistory.postHistory;
    return queryFactory
        .select(
            Projections.constructor(
                PostHistoryBaseResponse.class,
                postHistory.id,
                postHistory.lastModifiedDate,
                postHistory.modifyUserEmail,
                postHistory.lastModifiedName))
        .from(postHistory)
        .where(
            lastModifiedDateBetween(request.getModifyDateFrom(), request.getModifyDateTo()),
            modifyUserEmailOrName(request.getModifyUserEmailOrName()))
        .orderBy(postHistory.lastModifiedDate.desc());
  }

  private BooleanExpression lastModifiedDateBetween(LocalDateTime from, LocalDateTime to) {
    QPostHistory postHistory = QPostHistory.postHistory;
    if (from != null && to != null) {
      return postHistory.lastModifiedDate.between(from, to);
    }
    if (from != null) {
      return postHistory.lastModifiedDate.goe(from);
    }
    if (to != null) {
      return postHistory.lastModifiedDate.loe(to);
    }
    return null;
  }

  private BooleanExpression modifyUserEmailOrName(String modifyUserEmailOrName) {
    QPostHistory postHistory = QPostHistory.postHistory;
    if (modifyUserEmailOrName != null) {
      return postHistory
          .modifyUserEmail
          .likeIgnoreCase(modifyUserEmailOrName)
          .or(postHistory.lastModifiedName.likeIgnoreCase(modifyUserEmailOrName));
    }
    return null;
  }
}
