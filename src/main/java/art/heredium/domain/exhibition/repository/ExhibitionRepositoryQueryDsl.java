package art.heredium.domain.exhibition.repository;

import art.heredium.domain.exhibition.entity.Exhibition;
import art.heredium.domain.exhibition.model.dto.request.GetAdminExhibitionRequest;
import art.heredium.domain.exhibition.model.dto.request.GetUserExhibitionRequest;
import art.heredium.domain.exhibition.model.dto.response.GetAdminExhibitionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface ExhibitionRepositoryQueryDsl {
    Page<GetAdminExhibitionResponse> search(GetAdminExhibitionRequest dto, Pageable pageable);

    Slice<Exhibition> search(GetUserExhibitionRequest dto, Pageable pageable);

    List<Exhibition> searchByHome();
}
