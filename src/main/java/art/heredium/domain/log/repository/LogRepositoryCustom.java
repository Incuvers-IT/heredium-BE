package art.heredium.domain.log.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import art.heredium.domain.log.entity.Log;
import art.heredium.domain.log.model.dto.request.GetLogSearchRequest;

public interface LogRepositoryCustom {
  Page<Log> search(GetLogSearchRequest dto, Pageable pageable);

  Page<Log> search(GetLogSearchRequest dto, Long id, Pageable pageable);
}
