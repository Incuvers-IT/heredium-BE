package art.heredium.domain.coffee.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import art.heredium.domain.coffee.entity.CoffeeRound;

public interface CoffeeRoundRepository extends JpaRepository<CoffeeRound, Long> {
  @Override
  @EntityGraph(attributePaths = "coffee")
  Optional<CoffeeRound> findById(Long aLong);

  CoffeeRound findByCoffee_IdAndStartDate(Long coffee_id, LocalDateTime startDate);
}
