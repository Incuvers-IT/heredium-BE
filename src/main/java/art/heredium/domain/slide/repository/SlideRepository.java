package art.heredium.domain.slide.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import art.heredium.domain.slide.entity.Slide;

public interface SlideRepository extends JpaRepository<Slide, Long>, SlideRepositoryQueryDsl {

  @Query(
      "SELECT s FROM Slide s WHERE s.startDate <= CURRENT_TIMESTAMP AND s.endDate >= CURRENT_TIMESTAMP AND s.isEnabled IS TRUE ORDER BY s.order DESC")
  List<Slide> findPostingByUser();

  Slide findTop1ByOrderByOrderDesc();

  @Query("SELECT p FROM Popup p WHERE p.order >= :min AND p.order <= :max ORDER BY p.order DESC")
  List<Slide> findAllByOrder(@Param("min") Long min, @Param("max") Long max);
}
