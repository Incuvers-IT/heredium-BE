package art.heredium.domain.docent.repository;

import art.heredium.domain.docent.entity.Docent;
import art.heredium.domain.docent.model.dto.request.GetAdminDocentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocentRepositoryQueryDsl {
    Page<Docent> search(GetAdminDocentRequest dto, Pageable pageable);
}
