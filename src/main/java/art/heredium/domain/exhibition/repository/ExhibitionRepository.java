package art.heredium.domain.exhibition.repository;

import art.heredium.domain.exhibition.entity.Exhibition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExhibitionRepository extends JpaRepository<Exhibition, Long>, ExhibitionRepositoryQueryDsl {
    @Query(value = "SELECT * FROM exhibition e WHERE e.booking_date <= now() AND e.end_date >= now() AND e.is_enabled IS TRUE ORDER BY e.start_date DESC LIMIT 1", nativeQuery = true)
    Exhibition findByLastStartDate();

    @Query("SELECT e FROM Exhibition e WHERE CURRENT_TIMESTAMP between e.startDate AND e.endDate AND e.isEnabled IS TRUE ORDER BY e.startDate ASC, e.endDate ASC, e.createdDate ASC")
    List<Exhibition> findAllByProgress();

    @Query("SELECT e FROM Exhibition e WHERE e.endDate >= CURRENT_TIMESTAMP AND e.isEnabled IS TRUE ORDER BY e.startDate DESC, e.endDate DESC, e.createdDate DESC ")
    List<Exhibition> findAllByEndDateAfterNow();
}
