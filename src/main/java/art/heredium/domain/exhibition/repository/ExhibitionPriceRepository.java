package art.heredium.domain.exhibition.repository;

import art.heredium.domain.exhibition.entity.ExhibitionPrice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExhibitionPriceRepository extends JpaRepository<ExhibitionPrice, Long> {
    @Override
    @EntityGraph(attributePaths = "exhibition")
    Optional<ExhibitionPrice> findById(Long aLong);

    @Override
    @EntityGraph(attributePaths = "exhibition")
    List<ExhibitionPrice> findAllById(Iterable<Long> longs);
}