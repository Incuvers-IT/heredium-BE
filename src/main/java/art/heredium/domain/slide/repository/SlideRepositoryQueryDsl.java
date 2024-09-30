package art.heredium.domain.slide.repository;

import art.heredium.domain.slide.entity.Slide;
import art.heredium.domain.slide.model.dto.request.GetAdminSlideRequest;
import art.heredium.domain.slide.model.dto.request.PutAdminSlideOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SlideRepositoryQueryDsl {
    Page<Slide> search(GetAdminSlideRequest dto, Pageable pageable);

    List<Slide> search(PutAdminSlideOrderRequest dto, Long min, Long max);
}
