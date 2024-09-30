package art.heredium.domain.docent.repository;

import art.heredium.domain.common.type.DateState;
import art.heredium.domain.common.type.HallType;
import art.heredium.domain.docent.entity.Docent;
import art.heredium.domain.docent.model.dto.request.GetAdminDocentRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.tika.utils.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static art.heredium.domain.docent.entity.QDocent.docent;

@RequiredArgsConstructor
public class DocentRepositoryImpl implements DocentRepositoryQueryDsl {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Docent> search(GetAdminDocentRequest dto, Pageable pageable) {
        Long total = queryFactory
                .select(Wildcard.count)
                .from(docent)
                .where(searchFilter(dto))
                .fetch()
                .get(0);

        List<Docent> result = total > 0 ? queryFactory
                .selectFrom(docent)
                .where(searchFilter(dto))
                .orderBy(docent.startDate.desc(), docent.endDate.desc(), docent.lastModifiedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch() : new ArrayList<>();

        return new PageImpl<>(result, pageable, total);
    }

    private BooleanBuilder searchFilter(GetAdminDocentRequest dto) {
        BooleanBuilder builder = new BooleanBuilder();
        if (dto.getSearchDateType().equals(GetAdminDocentRequest.SearchDateType.CREATED_DATE)) {
            builder.and(createdDateGoe(dto.getStartDate()));
            builder.and(createdDateLoe(dto.getEndDate()));
        } else if (dto.getSearchDateType().equals(GetAdminDocentRequest.SearchDateType.LAST_MODIFIED_DATE)) {
            builder.and(lastModifiedDateGoe(dto.getStartDate()));
            builder.and(lastModifiedDateLoe(dto.getEndDate()));
        } else if (dto.getSearchDateType().equals(GetAdminDocentRequest.SearchDateType.SCHEDULE)) {
            builder.and(startDateGoe(dto.getStartDate()));
            builder.and(endDateLoe(dto.getEndDate()));
        }

        builder.and(hallJsonSearch(dto.getHall()));
        builder.and(enabledEq(dto.getIsEnabled()));

        if (dto.getState() != null && dto.getState().size() > 0) {
            BooleanBuilder stateBuilder = new BooleanBuilder();
            dto.getState().forEach(state -> stateBuilder.or(stateEq(state)));
            builder.and(stateBuilder);
        }

        if (!StringUtils.isBlank(dto.getText())) {
            BooleanBuilder textBuilder = new BooleanBuilder();
            textBuilder.or(titleContain(dto.getText()));
            textBuilder.or(createdNameContain(dto.getText()));
            textBuilder.or(lastModifiedNameContain(dto.getText()));
            builder.and(textBuilder);
        }
        return builder;
    }

    private BooleanBuilder userEnabledFilter() {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(enabledEq(true));
        return builder;
    }

    private BooleanExpression hallJsonSearch(HallType value) {
        return value != null ? Expressions.stringTemplate("JSON_SEARCH({0}, {1}, {2})", docent.halls, "one", value.name()).isNotNull() : null;
    }

    private BooleanExpression titleContain(String value) {
        return !StringUtils.isBlank(value) ? docent.title.contains(value) : null;
    }

    private BooleanExpression createdNameContain(String value) {
        return !StringUtils.isBlank(value) ? docent.createdName.contains(value) : null;
    }

    private BooleanExpression lastModifiedNameContain(String value) {
        return !StringUtils.isBlank(value) ? docent.lastModifiedName.contains(value) : null;
    }

    private BooleanExpression enabledEq(Boolean enabled) {
        return enabled != null ? docent.isEnabled.eq(enabled) : null;
    }

    private BooleanExpression createdDateGoe(LocalDateTime value) {
        return value != null ? docent.createdDate.goe(value) : null;
    }

    private BooleanExpression createdDateLoe(LocalDateTime value) {
        return value != null ? docent.createdDate.loe(value) : null;
    }

    private BooleanExpression lastModifiedDateGoe(LocalDateTime value) {
        return value != null ? docent.lastModifiedDate.goe(value) : null;
    }

    private BooleanExpression lastModifiedDateLoe(LocalDateTime value) {
        return value != null ? docent.lastModifiedDate.loe(value) : null;
    }

    private BooleanExpression startDateGoe(LocalDateTime value) {
        return value != null ? docent.startDate.goe(value) : null;
    }

    private BooleanExpression endDateLoe(LocalDateTime value) {
        return value != null ? docent.endDate.loe(value) : null;
    }

    private BooleanBuilder stateEq(DateState state) {
        BooleanBuilder builder = new BooleanBuilder();
        DateTimeExpression<LocalDateTime> now = DateTimeExpression.currentTimestamp(LocalDateTime.class);
        if (state.equals(DateState.SCHEDULE)) {
            builder.and(now.before(docent.startDate));
        } else if (state.equals(DateState.PROGRESS)) {
            builder.and(now.between(docent.startDate, docent.endDate));
        } else if (state.equals(DateState.TERMINATION)) {
            builder.and(now.after(docent.endDate));
        }
        return builder;
    }
}