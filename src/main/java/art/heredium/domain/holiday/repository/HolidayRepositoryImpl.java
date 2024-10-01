package art.heredium.domain.holiday.repository;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import art.heredium.domain.holiday.entity.Holiday;
import art.heredium.domain.holiday.entity.QHoliday;
import art.heredium.domain.holiday.model.dto.request.GetHolidayRequest;

@RequiredArgsConstructor
public class HolidayRepositoryImpl implements HolidayRepositoryQueryDsl {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Holiday> search(GetHolidayRequest dto) {
    List<Holiday> result =
        queryFactory
            .selectFrom(QHoliday.holiday)
            .where(searchFilter(dto))
            .orderBy(QHoliday.holiday.day.asc())
            .fetch();
    return result;
  }

  private BooleanBuilder searchFilter(GetHolidayRequest dto) {
    BooleanBuilder builder = new BooleanBuilder();

    builder.and(yearEq(dto.getYear()));
    builder.and(dayBetween(dto.getStartDate(), dto.getEndDate()));
    return builder;
  }

  private BooleanExpression yearEq(Integer value) {
    return value != null
        ? Expressions.numberTemplate(Integer.class, "YEAR({0})", QHoliday.holiday.day).eq(value)
        : null;
  }

  private BooleanExpression dayBetween(LocalDate startDate, LocalDate endDate) {
    return startDate != null && endDate != null
        ? QHoliday.holiday.day.between(startDate, endDate)
        : null;
  }
}
