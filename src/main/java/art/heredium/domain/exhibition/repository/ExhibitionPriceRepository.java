package art.heredium.domain.exhibition.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import art.heredium.domain.exhibition.entity.ExhibitionPrice;

public interface ExhibitionPriceRepository extends JpaRepository<ExhibitionPrice, Long> {
  @Override
  @EntityGraph(attributePaths = "exhibition")
  Optional<ExhibitionPrice> findById(Long aLong);

  @Override
  @EntityGraph(attributePaths = "exhibition")
  List<ExhibitionPrice> findAllById(Iterable<Long> longs);
}
