package art.heredium.domain.account.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import art.heredium.domain.account.model.dto.request.GetAdminHanaBankRequest;
import art.heredium.domain.account.model.dto.response.GetAdminHanaBankUserResponse;

public interface NonUserRepositoryQueryDsl {
  Page<GetAdminHanaBankUserResponse> search(GetAdminHanaBankRequest dto, Pageable pageable);
}
