package art.heredium.domain.account.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import art.heredium.domain.account.entity.Admin;

public interface AdminRepositoryQueryDsl {
  Page<Admin> search(Pageable pageable);
}
