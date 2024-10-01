package art.heredium.domain.popup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import art.heredium.domain.popup.entity.Popup;

public interface PopupRepository extends JpaRepository<Popup, Long>, PopupRepositoryQueryDsl {

  @Query(
      "SELECT p FROM Popup p WHERE p.startDate <= CURRENT_TIMESTAMP AND p.endDate >= CURRENT_TIMESTAMP AND p.isEnabled IS TRUE ORDER BY p.order DESC")
  List<Popup> findPostingByUser();

  Popup findTop1ByOrderByOrderDesc();

  @Query("SELECT p FROM Popup p WHERE p.order >= :min AND p.order <= :max ORDER BY p.order DESC")
  List<Popup> findAllByOrder(@Param("min") Long min, @Param("max") Long max);
}
