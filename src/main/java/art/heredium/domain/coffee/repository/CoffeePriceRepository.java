package art.heredium.domain.coffee.repository;

import art.heredium.domain.coffee.entity.CoffeePrice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoffeePriceRepository extends JpaRepository<CoffeePrice, Long> {
    @Override
    @EntityGraph(attributePaths = "coffee")
    Optional<CoffeePrice> findById(Long aLong);

    @Override
    @EntityGraph(attributePaths = "coffee")
    List<CoffeePrice> findAllById(Iterable<Long> longs);
}