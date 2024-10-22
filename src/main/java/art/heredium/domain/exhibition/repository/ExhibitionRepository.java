package art.heredium.domain.exhibition.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import art.heredium.domain.exhibition.entity.Exhibition;

public interface ExhibitionRepository
    extends JpaRepository<Exhibition, Long>, ExhibitionRepositoryQueryDsl {
  @Query(
      value =
          "SELECT * FROM exhibition e WHERE e.booking_date <= now() AND e.end_date >= now() AND e.is_enabled IS TRUE ORDER BY e.start_date DESC LIMIT 1",
      nativeQuery = true)
  Exhibition findByLastStartDate();

  @Query(
      "SELECT e FROM Exhibition e WHERE CURRENT_TIMESTAMP between e.startDate AND e.endDate AND e.isEnabled IS TRUE ORDER BY e.startDate ASC, e.endDate ASC, e.createdDate ASC")
  List<Exhibition> findAllByProgress();

  @Query(
      "SELECT e FROM Exhibition e WHERE e.endDate >= CURRENT_TIMESTAMP AND e.isEnabled IS TRUE ORDER BY e.startDate DESC, e.endDate DESC, e.createdDate DESC ")
  List<Exhibition> findAllByEndDateAfterNow();

  @Query(
      value =
          "SELECT * FROM exhibition e WHERE e.start_date > now() AND e.is_enabled IS TRUE ORDER BY e.start_date, e.end_date, e.created_date LIMIT :count",
      nativeQuery = true)
  List<Exhibition> findFirstXByFutureAndIsEnabledTrue(@Param("count") int count);

  @Query(
      value =
          "SELECT * FROM exhibition e WHERE e.start_date <= now() AND e.end_date >= now() AND e.is_enabled IS TRUE ORDER BY e.end_date LIMIT :count",
      nativeQuery = true)
  List<Exhibition> findFirstXByOngoingAndIsEnabledTrue(@Param("count") int count);

  @Query(
      value =
          "SELECT * FROM exhibition e WHERE e.end_date <= now() AND e.is_enabled IS TRUE ORDER BY e.start_date DESC, e.end_date DESC, e.created_date DESC LIMIT :count",
      nativeQuery = true)
  List<Exhibition> findFirstXByCompletedAndIsEnabledTrue(@Param("count") int count);
}
