package art.heredium.domain.policy.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import art.heredium.domain.policy.entity.Policy;
import art.heredium.domain.policy.model.dto.request.GetAdminPolicyRequest;

public interface PolicyRepositoryQueryDsl {
  Page<Policy> search(GetAdminPolicyRequest dto, Pageable pageable);
}
