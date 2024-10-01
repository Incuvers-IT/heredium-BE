package art.heredium.domain.common.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.tika.utils.StringUtils;

import art.heredium.domain.common.model.dto.request.GetUserCommonSearchRequest;
import art.heredium.domain.common.model.dto.response.GetUserCommonSearchContentResponse;
import art.heredium.domain.common.model.dto.response.GetUserCommonSearchIndexResponse;
import art.heredium.domain.common.model.dto.response.QGetUserCommonSearchContentResponse;
import art.heredium.domain.common.type.DateState;

import static art.heredium.domain.event.entity.QEvent.event;
import static art.heredium.domain.exhibition.entity.QExhibition.exhibition;
import static art.heredium.domain.notice.entity.QNotice.notice;
import static art.heredium.domain.program.entity.QProgram.program;

@Repository
@RequiredArgsConstructor
public class CommonRepository {

  private final JPAQueryFactory queryFactory;

  public Page<GetUserCommonSearchContentResponse> searchContent(
      GetUserCommonSearchRequest dto, Pageable pageable) {

    boolean isBlinkText = StringUtils.isBlank(dto.getText());
    Long total =
        isBlinkText
            ? 0L
            : queryFactory
                .select(Wildcard.count)
                .from(getSearchTable(dto))
                .where(searchFilter(dto))
                .fetch()
                .get(0);

    QGetUserCommonSearchContentResponse column = getSearchColumn(dto);
    List<GetUserCommonSearchContentResponse> result =
        total > 0
            ? queryFactory
                .select(column)
                .from(getSearchTable(dto))
                .where(searchFilter(dto))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getSearchOrder(dto))
                .fetch()
            : new ArrayList<>();

    return new PageImpl<>(result, pageable, total);
  }

  public List<GetUserCommonSearchIndexResponse> searchIndex(String text) {
    return Arrays.stream(GetUserCommonSearchRequest.SearchDateType.values())
        .map(type -> new GetUserCommonSearchIndexResponse(type, getSearchIndexCount(text, type)))
        .collect(Collectors.toList());
  }

  private Long getSearchIndexCount(String text, GetUserCommonSearchRequest.SearchDateType type) {
    boolean isBlinkText = StringUtils.isBlank(text);
    if (isBlinkText) {
      return 0L;
    }
    GetUserCommonSearchRequest dto = new GetUserCommonSearchRequest(text, type);
    return queryFactory
        .select(Wildcard.count)
        .from(getSearchTable(dto))
        .where(searchFilter(dto))
        .fetch()
        .get(0);
  }

  private EntityPath getSearchTable(GetUserCommonSearchRequest dto) {
    if (dto.getType().equals(GetUserCommonSearchRequest.SearchDateType.EXHIBITION)) {
      return exhibition;
    } else if (dto.getType().equals(GetUserCommonSearchRequest.SearchDateType.PROGRAM)) {
      return program;
    } else if (dto.getType().equals(GetUserCommonSearchRequest.SearchDateType.EVENT)) {
      return event;
    } else if (dto.getType().equals(GetUserCommonSearchRequest.SearchDateType.NOTICE)) {
      return notice;
    }
    return null;
  }

  private OrderSpecifier<?>[] getSearchOrder(GetUserCommonSearchRequest dto) {
    DateTimeExpression<LocalDateTime> now =
        DateTimeExpression.currentTimestamp(LocalDateTime.class);
    if (dto.getType().equals(GetUserCommonSearchRequest.SearchDateType.EXHIBITION)) {
      NumberExpression<Integer> state =
          new CaseBuilder()
              .when(now.before(exhibition.startDate).and(now.before(exhibition.bookingDate)))
              .then(DateState.SCHEDULE.getCode())
              .when(now.after(exhibition.endDate))
              .then(DateState.TERMINATION.getCode())
              .otherwise(DateState.PROGRESS.getCode());
      return new OrderSpecifier<?>[] {
        state.asc(),
        exhibition.startDate.desc(),
        exhibition.endDate.desc(),
        exhibition.lastModifiedDate.desc()
      };
    } else if (dto.getType().equals(GetUserCommonSearchRequest.SearchDateType.PROGRAM)) {
      NumberExpression<Integer> state =
          new CaseBuilder()
              .when(now.before(program.startDate).and(now.before(program.bookingDate)))
              .then(DateState.SCHEDULE.getCode())
              .when(now.after(program.endDate))
              .then(DateState.TERMINATION.getCode())
              .otherwise(DateState.PROGRESS.getCode());
      return new OrderSpecifier<?>[] {
        state.asc(),
        program.startDate.desc(),
        program.endDate.desc(),
        program.lastModifiedDate.desc()
      };
    } else if (dto.getType().equals(GetUserCommonSearchRequest.SearchDateType.EVENT)) {
      NumberExpression<Integer> state =
          new CaseBuilder()
              .when(now.before(event.startDate))
              .then(DateState.SCHEDULE.getCode())
              .when(now.after(event.endDate))
              .then(DateState.TERMINATION.getCode())
              .otherwise(DateState.PROGRESS.getCode());
      return new OrderSpecifier<?>[] {
        state.asc(), event.startDate.desc(), event.endDate.desc(), event.lastModifiedDate.desc()
      };
    } else if (dto.getType().equals(GetUserCommonSearchRequest.SearchDateType.NOTICE)) {
      return new OrderSpecifier<?>[] {notice.isNotice.desc(), notice.postDate.desc()};
    }
    return null;
  }

  private QGetUserCommonSearchContentResponse getSearchColumn(GetUserCommonSearchRequest dto) {
    if (dto.getType().equals(GetUserCommonSearchRequest.SearchDateType.EXHIBITION)) {
      return new QGetUserCommonSearchContentResponse(
          exhibition.id,
          exhibition.title,
          exhibition.subtitle,
          exhibition.thumbnail,
          exhibition.createdDate,
          exhibition.startDate,
          exhibition.endDate);
    } else if (dto.getType().equals(GetUserCommonSearchRequest.SearchDateType.PROGRAM)) {
      return new QGetUserCommonSearchContentResponse(
          program.id,
          program.title,
          program.subtitle,
          program.thumbnail,
          program.createdDate,
          program.startDate,
          program.endDate);
    } else if (dto.getType().equals(GetUserCommonSearchRequest.SearchDateType.EVENT)) {
      return new QGetUserCommonSearchContentResponse(
          event.id,
          event.title,
          event.thumbnail,
          event.createdDate,
          event.startDate,
          event.endDate);
    } else if (dto.getType().equals(GetUserCommonSearchRequest.SearchDateType.NOTICE)) {
      return new QGetUserCommonSearchContentResponse(
          notice.id, notice.title, notice.isNotice, notice.postDate);
    }
    return null;
  }

  private BooleanBuilder searchFilter(GetUserCommonSearchRequest dto) {
    BooleanBuilder builder = new BooleanBuilder();
    if (!StringUtils.isBlank(dto.getText())) {
      if (dto.getType().equals(GetUserCommonSearchRequest.SearchDateType.EXHIBITION)) {
        builder.and(exhibitionTitleContain(dto.getText()));
        builder.and(exhibitionEnabledEq(true));
      } else if (dto.getType().equals(GetUserCommonSearchRequest.SearchDateType.PROGRAM)) {
        builder.and(programTitleContain(dto.getText()));
        builder.and(programEnabledEq(true));
      } else if (dto.getType().equals(GetUserCommonSearchRequest.SearchDateType.EVENT)) {
        builder.and(eventTitleContain(dto.getText()));
        builder.and(eventEnabledEq(true));
      } else if (dto.getType().equals(GetUserCommonSearchRequest.SearchDateType.NOTICE)) {
        builder.and(noticeTitleContain(dto.getText()));
        builder.and(noticeEnabledEq(true));
        builder.and(noticePostDateBefore());
      }
    }
    return builder;
  }

  private BooleanExpression exhibitionTitleContain(String value) {
    return value != null ? exhibition.title.contains(value) : null;
  }

  private BooleanExpression exhibitionEnabledEq(Boolean value) {
    return value != null ? exhibition.isEnabled.eq(value) : null;
  }

  private BooleanExpression programTitleContain(String value) {
    return value != null ? program.title.contains(value) : null;
  }

  private BooleanExpression programEnabledEq(Boolean value) {
    return value != null ? program.isEnabled.eq(value) : null;
  }

  private BooleanExpression eventTitleContain(String value) {
    return value != null ? event.title.contains(value) : null;
  }

  private BooleanExpression eventEnabledEq(Boolean value) {
    return value != null ? event.isEnabled.eq(value) : null;
  }

  private BooleanExpression noticeTitleContain(String value) {
    return value != null ? notice.title.contains(value) : null;
  }

  private BooleanExpression noticeEnabledEq(Boolean value) {
    return value != null ? notice.isEnabled.eq(value) : null;
  }

  private BooleanExpression noticePostDateBefore() {
    return notice.postDate.before(DateTimeExpression.currentTimestamp(LocalDateTime.class));
  }
}
