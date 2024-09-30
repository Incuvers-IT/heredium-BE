package art.heredium.domain.account.repository;

import art.heredium.domain.account.model.dto.request.GetAdminHanaBankRequest;
import art.heredium.domain.account.model.dto.response.GetAdminHanaBankUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NonUserRepositoryQueryDsl {
    Page<GetAdminHanaBankUserResponse> search(GetAdminHanaBankRequest dto, Pageable pageable);
}