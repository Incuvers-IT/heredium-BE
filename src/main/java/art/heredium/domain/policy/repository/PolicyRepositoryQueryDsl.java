package art.heredium.domain.policy.repository;

import art.heredium.domain.policy.model.dto.request.GetAdminPolicyRequest;
import art.heredium.domain.policy.entity.Policy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PolicyRepositoryQueryDsl {
    Page<Policy> search(GetAdminPolicyRequest dto, Pageable pageable);
}
