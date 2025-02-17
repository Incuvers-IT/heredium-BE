package art.heredium.domain.program.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import art.heredium.domain.program.entity.Program;

public interface ProgramRepository extends JpaRepository<Program, Long>, ProgramRepositoryQueryDsl {
  @Query(
      value =
          "SELECT * FROM program e WHERE e.booking_date <= now() AND e.end_date >= now() AND e.is_enabled IS TRUE ORDER BY e.start_date DESC LIMIT 1",
      nativeQuery = true)
  Program findByLastStartDate();

  @Query(
      "SELECT e FROM Program e WHERE CURRENT_TIMESTAMP between e.startDate AND e.endDate AND e.isEnabled IS TRUE ORDER BY e.startDate ASC, e.endDate ASC, e.createdDate ASC")
  List<Program> findAllByProgress();

  @Query(
      "SELECT e FROM Program e WHERE e.endDate >= CURRENT_TIMESTAMP AND e.isEnabled IS TRUE ORDER BY e.startDate DESC, e.endDate DESC, e.createdDate DESC")
  List<Program> findAllByEndDateAfterNow();

  @Query(
      value =
          "SELECT * FROM program p WHERE p.start_date > now() AND p.is_enabled IS TRUE ORDER BY p.end_date LIMIT :count",
      nativeQuery = true)
  List<Program> findFirstXByFutureAndIsEnabledTrue(@Param("count") int count);

  @Query(
      value =
          "SELECT * FROM program p WHERE p.start_date <= now() AND p.end_date >= now() AND p.is_enabled IS TRUE ORDER BY p.start_date DESC, p.end_date DESC, p.created_date DESC LIMIT :count",
      nativeQuery = true)
  List<Program> findFirstXByOngoingAndIsEnabledTrue(@Param("count") int count);

  @Query(
      value =
          "SELECT * FROM program p WHERE p.end_date <= now() AND p.is_enabled IS TRUE ORDER BY p.start_date DESC, p.end_date DESC, p.created_date DESC LIMIT :count",
      nativeQuery = true)
  List<Program> findFirstXByCompletedAndIsEnabledTrue(@Param("count") int count);
}
