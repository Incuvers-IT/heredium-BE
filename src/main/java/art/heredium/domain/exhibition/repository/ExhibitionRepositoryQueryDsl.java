package art.heredium.domain.exhibition.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import art.heredium.domain.exhibition.entity.Exhibition;
import art.heredium.domain.exhibition.model.dto.request.GetAdminExhibitionRequest;
import art.heredium.domain.exhibition.model.dto.request.GetUserExhibitionRequest;
import art.heredium.domain.exhibition.model.dto.response.GetAdminExhibitionResponse;

public interface ExhibitionRepositoryQueryDsl {
  Page<GetAdminExhibitionResponse> search(GetAdminExhibitionRequest dto, Pageable pageable);

  Slice<Exhibition> search(GetUserExhibitionRequest dto, Pageable pageable);

  List<Exhibition> searchByHome();
}
