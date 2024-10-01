package art.heredium.domain.notice.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.tika.utils.StringUtils;

import art.heredium.domain.notice.entity.Notice;
import art.heredium.domain.notice.model.dto.request.GetAdminNoticeRequest;

import static art.heredium.domain.notice.entity.QNotice.notice;

@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryQueryDsl {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Notice> search(GetAdminNoticeRequest dto, Pageable pageable) {
    Long total =
        queryFactory.select(Wildcard.count).from(notice).where(searchFilter(dto)).fetch().get(0);

    List<Notice> result =
        total > 0
            ? queryFactory
                .selectFrom(notice)
                .where(searchFilter(dto))
                .orderBy(
                    notice.isNotice.desc(), notice.postDate.desc(), notice.lastModifiedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
            : new ArrayList<>();
    return new PageImpl<>(result, pageable, total);
  }

  @Override
  public Page<Notice> search(Pageable pageable) {
    Long total =
        queryFactory.select(Wildcard.count).from(notice).where(userEnabledFilter()).fetch().get(0);

    List<Notice> result =
        total > 0
            ? queryFactory
                .selectFrom(notice)
                .where(userEnabledFilter())
                .orderBy(
                    notice.isNotice.desc(), notice.postDate.desc(), notice.lastModifiedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
            : new ArrayList<>();
    return new PageImpl<>(result, pageable, total);
  }

  @Override
  public List<Notice> home() {
    return queryFactory
        .selectFrom(notice)
        .where(userEnabledFilter())
        .orderBy(notice.postDate.desc(), notice.lastModifiedDate.desc())
        .limit(5)
        .fetch();
  }

  private BooleanBuilder searchFilter(GetAdminNoticeRequest dto) {
    BooleanBuilder builder = new BooleanBuilder();

    if (!StringUtils.isBlank(dto.getText())) {
      BooleanBuilder textFilter = new BooleanBuilder();
      textFilter.or(titleContain(dto.getText()));
      textFilter.or(createdNameContain(dto.getText()));
      textFilter.or(lastModifiedNameContain(dto.getText()));
      builder.and(textFilter);
    }
    return builder;
  }

  private BooleanBuilder userEnabledFilter() {
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(enabledEq(true));
    builder.and(postDateBefore());
    return builder;
  }

  private BooleanExpression titleContain(String value) {
    return !StringUtils.isBlank(value) ? notice.title.contains(value) : null;
  }

  private BooleanExpression createdNameContain(String value) {
    return !StringUtils.isBlank(value) ? notice.createdName.contains(value) : null;
  }

  private BooleanExpression lastModifiedNameContain(String value) {
    return !StringUtils.isBlank(value) ? notice.lastModifiedName.contains(value) : null;
  }

  private BooleanExpression enabledEq(Boolean enabled) {
    return enabled != null ? notice.isEnabled.eq(enabled) : null;
  }

  private BooleanExpression postDateBefore() {
    return notice.postDate.before(DateTimeExpression.currentTimestamp(LocalDateTime.class));
  }
}
