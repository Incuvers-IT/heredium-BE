package art.heredium.domain.event.repository;

import art.heredium.domain.common.type.DateState;
import art.heredium.domain.event.entity.QEvent;
import art.heredium.domain.event.entity.Event;
import art.heredium.domain.event.model.dto.request.GetAdminEventRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.tika.utils.StringUtils;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepositoryQueryDsl {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Event> home(GetAdminEventRequest dto, Pageable pageable) {
        Long total = queryFactory
                .select(Wildcard.count)
                .from(QEvent.event)
                .where(searchFilter(dto))
                .fetch()
                .get(0);

        List<Event> result = total > 0 ? queryFactory
                .selectFrom(QEvent.event)
                .where(searchFilter(dto))
                .orderBy(QEvent.event.startDate.desc(), QEvent.event.lastModifiedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch() : new ArrayList<>();
        return new PageImpl<>(result, pageable, total);
    }

    @Override
    public Slice<Event> home(Pageable pageable) {
        DateTimeExpression<LocalDateTime> now = DateTimeExpression.currentTimestamp(LocalDateTime.class);
        NumberExpression<Integer> state = new CaseBuilder()
                .when(now.before(QEvent.event.startDate))
                .then(DateState.SCHEDULE.getCode())
                .when(now.after(QEvent.event.endDate))
                .then(DateState.TERMINATION.getCode())
                .otherwise(DateState.PROGRESS.getCode());

        List<Event> content = queryFactory
                .selectFrom(QEvent.event)
                .where(enabledEq(true))
                .orderBy(state.asc(), QEvent.event.startDate.desc(), QEvent.event.endDate.desc(), QEvent.event.lastModifiedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    @Override
    public List<Event> home() {
        DateTimeExpression<LocalDateTime> now = DateTimeExpression.currentTimestamp(LocalDateTime.class);
        NumberExpression<Integer> state = new CaseBuilder()
                .when(now.before(QEvent.event.startDate))
                .then(DateState.SCHEDULE.getCode())
                .when(now.after(QEvent.event.endDate))
                .then(DateState.TERMINATION.getCode())
                .otherwise(DateState.PROGRESS.getCode());

        return queryFactory
                .selectFrom(QEvent.event)
                .where(isProgress(true), enabledEq(true))
                .orderBy(state.asc(), QEvent.event.startDate.desc(), QEvent.event.endDate.desc(), QEvent.event.lastModifiedDate.desc())
                .limit(2)
                .fetch();
    }

    private BooleanBuilder searchFilter(GetAdminEventRequest dto) {
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

    private BooleanExpression titleContain(String value) {
        return !StringUtils.isBlank(value) ? QEvent.event.title.contains(value) : null;
    }

    private BooleanExpression createdNameContain(String value) {
        return !StringUtils.isBlank(value) ? QEvent.event.createdName.contains(value) : null;
    }

    private BooleanExpression lastModifiedNameContain(String value) {
        return !StringUtils.isBlank(value) ? QEvent.event.lastModifiedName.contains(value) : null;
    }

    private BooleanExpression enabledEq(Boolean enabled) {
        return enabled != null ? QEvent.event.isEnabled.eq(enabled) : null;
    }

    private BooleanExpression isProgress(Boolean isProgress) {
        return isProgress
                ? QEvent.event.endDate.goe(DateTimeExpression.currentTimestamp(LocalDateTime.class))
                : QEvent.event.endDate.before(DateTimeExpression.currentTimestamp(LocalDateTime.class));
    }
}