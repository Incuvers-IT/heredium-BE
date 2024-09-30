package art.heredium.domain.coffee.repository;

import art.heredium.domain.coffee.entity.CoffeeRound;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CoffeeRoundRepository extends JpaRepository<CoffeeRound, Long> {
    @Override
    @EntityGraph(attributePaths = "coffee")
    Optional<CoffeeRound> findById(Long aLong);

    CoffeeRound findByCoffee_IdAndStartDate(Long coffee_id, LocalDateTime startDate);
}
