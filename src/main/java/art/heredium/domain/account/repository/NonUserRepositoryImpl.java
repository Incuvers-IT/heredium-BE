package art.heredium.domain.account.repository;

import art.heredium.domain.account.model.dto.request.GetAdminHanaBankRequest;
import art.heredium.domain.account.model.dto.response.GetAdminHanaBankUserResponse;
import art.heredium.domain.account.model.dto.response.QGetAdminHanaBankUserResponse;
import art.heredium.domain.ticket.entity.QTicket;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.domain.ticket.type.TicketType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.tika.utils.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static art.heredium.domain.account.entity.QNonUser.nonUser;

@RequiredArgsConstructor
public class NonUserRepositoryImpl implements NonUserRepositoryQueryDsl {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<GetAdminHanaBankUserResponse> search(GetAdminHanaBankRequest dto, Pageable pageable) {
        Long total = queryFactory
                .select(Wildcard.count)
                .from(nonUser)
                .where(searchFilter(dto))
                .fetch()
                .get(0);

        List<GetAdminHanaBankUserResponse> result = total > 0 ? getGetAdminAccountQuery(dto)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch() : new ArrayList<>();

        return new PageImpl<>(result, pageable, total);
    }

    private JPQLQuery<Long> selectVisitCount() {
        return JPAExpressions.select(Wildcard.count)
                .from(QTicket.ticket)
                .where(QTicket.ticket.nonUser.id.eq(nonUser.id), QTicket.ticket.kind.eq(TicketKindType.EXHIBITION), QTicket.ticket.type.ne(TicketType.INVITE), QTicket.ticket.state.eq(TicketStateType.USED));
    }


    private JPAQuery<GetAdminHanaBankUserResponse> getGetAdminAccountQuery(GetAdminHanaBankRequest dto) {
        NumberPath<Long> visitCount = Expressions.numberPath(Long.class, "visitCount");
        return queryFactory
                .select(new QGetAdminHanaBankUserResponse(
                        nonUser.id,
                        nonUser.name,
                        nonUser.hanaBankUuid,
                        ExpressionUtils.as(selectVisitCount(), visitCount)
                ))
                .from(nonUser)
                .where(searchFilter(dto))
                .orderBy(nonUser.createdDate.desc());
    }

    private BooleanBuilder searchFilter(GetAdminHanaBankRequest dto) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(nameNotNull());
        if (!StringUtils.isBlank(dto.getText())) {
            BooleanBuilder textBuilder = new BooleanBuilder();
            textBuilder.or(nameContain(dto.getText()));
            textBuilder.or(hanaBankUuidContain(dto.getText()));
            builder.and(textBuilder);
        }
        return builder;
    }

    private BooleanExpression nameNotNull() {
        return nonUser.name.isNotNull();
    }

    private BooleanExpression nameContain(String value) {
        return value != null ? nonUser.name.contains(value) : null;
    }
    private BooleanExpression hanaBankUuidContain(String value) {
        return value != null ? nonUser.hanaBankUuid.contains(value) : null;
    }
}