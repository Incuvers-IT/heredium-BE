package art.heredium.domain.program.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import art.heredium.domain.program.entity.Program;
import art.heredium.domain.program.model.dto.request.GetAdminProgramRequest;
import art.heredium.domain.program.model.dto.request.GetUserProgramRequest;
import art.heredium.domain.program.model.dto.response.GetAdminProgramResponse;

public interface ProgramRepositoryQueryDsl {
  Page<GetAdminProgramResponse> search(GetAdminProgramRequest dto, Pageable pageable);

  Slice<Program> search(GetUserProgramRequest dto, Pageable pageable);

  List<Program> searchByHome();
}
