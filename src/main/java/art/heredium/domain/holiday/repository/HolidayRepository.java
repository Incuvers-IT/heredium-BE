package art.heredium.domain.holiday.repository;

import art.heredium.domain.holiday.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long>, HolidayRepositoryQueryDsl {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Holiday h WHERE h.day IN (:holiday)")
    void deleteAllByDayIn(@Param("holiday") Collection<LocalDate> holiday);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "INSERT IGNORE INTO holiday(day) VALUES (:localDate)", nativeQuery = true)
    void saveIgnore(@Param("localDate") LocalDate localDate);

    @Query("SELECT h.day FROM Holiday h WHERE h.day BETWEEN :startDate AND :endDate")
    List<LocalDate> findAllByDayBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    boolean existsByDay(LocalDate day);
}
