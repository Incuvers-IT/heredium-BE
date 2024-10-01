package art.heredium.domain.coffee.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import art.heredium.domain.coffee.entity.Coffee;

public interface CoffeeRepository extends JpaRepository<Coffee, Long>, CoffeeRepositoryQueryDsl {
  @Query(
      value =
          "SELECT * FROM coffee e WHERE e.booking_date <= now() AND e.end_date >= now() AND e.is_enabled IS TRUE ORDER BY e.start_date DESC LIMIT 1",
      nativeQuery = true)
  Coffee findByLastStartDate();

  @Query(
      "SELECT e FROM Coffee e WHERE CURRENT_TIMESTAMP between e.startDate AND e.endDate AND e.isEnabled IS TRUE ORDER BY e.startDate ASC, e.endDate ASC, e.createdDate ASC")
  List<Coffee> findAllByProgress();

  @Query(
      "SELECT e FROM Coffee e WHERE e.endDate >= CURRENT_TIMESTAMP AND e.isEnabled IS TRUE ORDER BY e.startDate DESC, e.endDate DESC, e.createdDate DESC ")
  List<Coffee> findAllByEndDateAfterNow();
}
