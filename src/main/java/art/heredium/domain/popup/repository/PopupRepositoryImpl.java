package art.heredium.domain.popup.repository;

import art.heredium.domain.popup.entity.QPopup;
import art.heredium.domain.popup.entity.Popup;
import art.heredium.domain.popup.model.dto.request.GetAdminPopupRequest;
import art.heredium.domain.popup.model.dto.request.PutAdminPopupOrderRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.tika.utils.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class PopupRepositoryImpl implements PopupRepositoryQueryDsl {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Popup> search(GetAdminPopupRequest dto, Pageable pageable) {
        Long total = queryFactory
                .select(Wildcard.count)
                .from(QPopup.popup)
                .where(searchFilter(dto))
                .fetch()
                .get(0);

        List<Popup> result = total > 0 ? queryFactory
                .selectFrom(QPopup.popup)
                .where(searchFilter(dto))
                .orderBy(orderByFilter(dto), QPopup.popup.lastModifiedDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch() : new ArrayList<>();
        return new PageImpl<>(result, pageable, total);
    }

    @Override
    public List<Popup> search(PutAdminPopupOrderRequest dto, Long min, Long max) {
        List<Popup> result = queryFactory
                .selectFrom(QPopup.popup)
                .where(searchFilter(dto), orderBetween(min, max))
                .orderBy(orderByFilter(dto), QPopup.popup.lastModifiedDate.desc())
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
        return QPopup.popup.order.between(min, max);
    }

    private OrderSpecifier orderByFilter(GetAdminPopupRequest dto) {
        return dto.getIsProgress() ? QPopup.popup.order.desc() : QPopup.popup.startDate.desc();
    }

    private BooleanBuilder searchFilter(GetAdminPopupRequest dto) {
        BooleanBuilder builder = new BooleanBuilder();

        if (!StringUtils.isBlank(dto.getText())) {
            BooleanBuilder textFilter = new BooleanBuilder();
            textFilter.or(titleContain(dto.getText()));
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
                ? QPopup.popup.endDate.goe(DateTimeExpression.currentTimestamp(LocalDateTime.class))
                : QPopup.popup.endDate.before(DateTimeExpression.currentTimestamp(LocalDateTime.class));
    }

    private BooleanExpression titleContain(String value) {
        return !StringUtils.isBlank(value) ? QPopup.popup.title.contains(value) : null;
    }

    private BooleanExpression enabledEq(Boolean value) {
        return value != null ? QPopup.popup.isEnabled.eq(value) : null;
    }

    private BooleanExpression dateBetween() {
        return DateTimeExpression.currentTimestamp(LocalDateTime.class).between(QPopup.popup.startDate, QPopup.popup.endDate);
    }
}