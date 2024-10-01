package art.heredium.domain.log.repository;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.tika.utils.StringUtils;

import art.heredium.domain.log.entity.Log;
import art.heredium.domain.log.entity.QLog;
import art.heredium.domain.log.model.dto.request.GetLogSearchRequest;
import art.heredium.domain.log.type.LogAction;
import art.heredium.domain.log.type.LogSearchType;
import art.heredium.domain.log.type.LogType;

@RequiredArgsConstructor
public class LogRepositoryImpl implements LogRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Log> search(GetLogSearchRequest dto, Pageable pageable) {
    Long total =
        queryFactory.select(Wildcard.count).from(QLog.log).where(searchFilter(dto)).fetch().get(0);

    List<Log> result =
        total > 0
            ? queryFactory
                .selectFrom(QLog.log)
                .where(searchFilter(dto))
                .orderBy(QLog.log.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
            : new ArrayList<>();

    return new PageImpl<>(result, pageable, total);
  }

  @Override
  public Page<Log> search(GetLogSearchRequest dto, Long id, Pageable pageable) {
    JPAQuery<?> query = queryFactory.from(QLog.log).where(searchFilter(dto), idEqual(id));

    Long total = query.select(Wildcard.count).fetch().get(0);
    List<Log> result =
        total > 0
            ? query
                .select(QLog.log)
                .orderBy(QLog.log.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
            : new ArrayList<>();

    return new PageImpl<>(result, pageable, total);
  }

  private BooleanBuilder searchFilter(GetLogSearchRequest dto) {
    BooleanBuilder builder = new BooleanBuilder();

    if (dto.getAction() != null && dto.getAction().size() > 0) {
      builder.and(actionIn(dto.getAction()));
    }

    if (!StringUtils.isBlank(dto.getText())) {
      BooleanBuilder orBuilder = new BooleanBuilder();
      LogSearchType type = dto.getType();
      if (type.equals(LogSearchType.ALL) || type.equals(LogSearchType.EMAIL)) {
        orBuilder.or(usernameContains(dto.getText()));
      }
      if (type.equals(LogSearchType.ALL) || type.equals(LogSearchType.NAME)) {
        orBuilder.or(nameContains(dto.getText()));
      }
      if (type.equals(LogSearchType.ALL) || type.equals(LogSearchType.TYPE)) {
        orBuilder.or(typeEqual(LogType.containsDesc(dto.getText())));
      }
      builder.and(orBuilder);
    }
    return builder;
  }

  private BooleanExpression idEqual(Long value) {
    return value != null ? QLog.log.admin.id.eq(value) : null;
  }

  private BooleanExpression actionIn(List<LogAction> value) {
    return value != null ? QLog.log.action.in(value) : null;
  }

  private BooleanExpression usernameContains(String value) {
    return value != null ? QLog.log.email.contains(value) : null;
  }

  private BooleanExpression nameContains(String value) {
    return value != null ? QLog.log.name.contains(value) : null;
  }

  private BooleanExpression typeEqual(LogType value) {
    return value != null ? QLog.log.type.eq(value) : null;
  }
}
