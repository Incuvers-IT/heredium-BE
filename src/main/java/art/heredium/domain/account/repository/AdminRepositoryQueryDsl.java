package art.heredium.domain.account.repository;

import art.heredium.domain.account.entity.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminRepositoryQueryDsl {
    Page<Admin> search(Pageable pageable);
}