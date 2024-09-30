package art.heredium.domain.account.repository;

import art.heredium.domain.account.entity.QAdmin;
import art.heredium.domain.account.entity.QAdminInfo;
import art.heredium.domain.account.entity.Admin;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class AdminRepositoryImpl implements AdminRepositoryQueryDsl {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Admin> search(Pageable pageable) {
        Long total = queryFactory
                .select(Wildcard.count)
                .from(QAdmin.admin)
                .fetch()
                .get(0);

        List<Admin> result = total > 0 ? queryFactory
                .selectFrom(QAdmin.admin)
                .innerJoin(QAdmin.admin.adminInfo, QAdminInfo.adminInfo).fetchJoin()
                .orderBy(QAdminInfo.adminInfo.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch() : new ArrayList<>();

        return new PageImpl<>(result, pageable, total);
    }
}