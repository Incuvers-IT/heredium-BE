package art.heredium.domain.slide.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.tika.utils.StringUtils;

import art.heredium.domain.slide.entity.QSlide;
import art.heredium.domain.slide.entity.Slide;
import art.heredium.domain.slide.model.dto.request.GetAdminSlideRequest;
import art.heredium.domain.slide.model.dto.request.PutAdminSlideOrderRequest;

@RequiredArgsConstructor
public class SlideRepositoryImpl implements SlideRepositoryQueryDsl {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Slide> search(GetAdminSlideRequest dto, Pageable pageable) {
    Long total =
        queryFactory
            .select(Wildcard.count)
            .from(QSlide.slide)
            .where(searchFilter(dto))
            .fetch()
            .get(0);

    List<Slide> result =
        total > 0
            ? queryFactory
                .selectFrom(QSlide.slide)
                .where(searchFilter(dto))
                .orderBy(orderByFilter(dto), QSlide.slide.lastModifiedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
            : new ArrayList<>();
    return new PageImpl<>(result, pageable, total);
  }

  @Override
  public List<Slide> search(PutAdminSlideOrderRequest dto, Long min, Long max) {
    List<Slide> result =
        queryFactory
            .selectFrom(QSlide.slide)
            .where(searchFilter(dto), orderBetween(min, max))
            .orderBy(orderByFilter(dto), QSlide.slide.lastModifiedDate.desc())
            .fetch();
    return result;
  }

  private BooleanBuilder userEnabledFilter() {
    BooleanBuilder builder = new BooleanBuilder();
    builder.and(enabledEq(true));
    builder.and(dateBetween());
    return builder;
  }

  private BooleanExpression orderBetween(Long min, Long max) {
    return QSlide.slide.order.between(min, max);
  }

  private OrderSpecifier orderByFilter(GetAdminSlideRequest dto) {
    return dto.getIsProgress() ? QSlide.slide.order.desc() : QSlide.slide.startDate.desc();
  }

  private BooleanBuilder searchFilter(GetAdminSlideRequest dto) {
    BooleanBuilder builder = new BooleanBuilder();

    if (!StringUtils.isBlank(dto.getText())) {
      BooleanBuilder textFilter = new BooleanBuilder();
      textFilter.or(titleContain(dto.getText()));
      textFilter.or(createdNameContain(dto.getText()));
      textFilter.or(lastModifiedNameContain(dto.getText()));
      builder.and(textFilter);
    }
    builder.and(isProgress(dto.getIsProgress()));
    if (dto.getIsProgress() && !Boolean.TRUE.equals(dto.getIsShowDisabled())) {
      builder.and(enabledEq(true));
    }
    return builder;
  }

  private BooleanExpression isProgress(Boolean isProgress) {
    return isProgress
        ? QSlide.slide.endDate.goe(DateTimeExpression.currentTimestamp(LocalDateTime.class))
        : QSlide.slide.endDate.before(DateTimeExpression.currentTimestamp(LocalDateTime.class));
  }

  private BooleanExpression titleContain(String value) {
    return !StringUtils.isBlank(value) ? QSlide.slide.title.contains(value) : null;
  }

  private BooleanExpression createdNameContain(String value) {
    return !StringUtils.isBlank(value) ? QSlide.slide.createdName.contains(value) : null;
  }

  private BooleanExpression lastModifiedNameContain(String value) {
    return !StringUtils.isBlank(value) ? QSlide.slide.lastModifiedName.contains(value) : null;
  }

  private BooleanExpression enabledEq(Boolean value) {
    return value != null ? QSlide.slide.isEnabled.eq(value) : null;
  }

  private BooleanExpression dateBetween() {
    return DateTimeExpression.currentTimestamp(LocalDateTime.class)
        .between(QSlide.slide.startDate, QSlide.slide.endDate);
  }
}
