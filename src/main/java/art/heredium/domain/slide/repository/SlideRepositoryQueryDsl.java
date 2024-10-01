package art.heredium.domain.slide.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import art.heredium.domain.slide.entity.Slide;
import art.heredium.domain.slide.model.dto.request.GetAdminSlideRequest;
import art.heredium.domain.slide.model.dto.request.PutAdminSlideOrderRequest;

public interface SlideRepositoryQueryDsl {
  Page<Slide> search(GetAdminSlideRequest dto, Pageable pageable);

  List<Slide> search(PutAdminSlideOrderRequest dto, Long min, Long max);
}
