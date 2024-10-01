package art.heredium.domain.docent.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import art.heredium.domain.docent.entity.Docent;
import art.heredium.domain.docent.model.dto.request.GetAdminDocentRequest;

public interface DocentRepositoryQueryDsl {
  Page<Docent> search(GetAdminDocentRequest dto, Pageable pageable);
}
