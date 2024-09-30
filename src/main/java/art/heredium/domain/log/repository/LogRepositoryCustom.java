package art.heredium.domain.log.repository;

import art.heredium.domain.log.entity.Log;
import art.heredium.domain.log.model.dto.request.GetLogSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LogRepositoryCustom {
    Page<Log> search(GetLogSearchRequest dto, Pageable pageable);

    Page<Log> search(GetLogSearchRequest dto, Long id, Pageable pageable);
}
