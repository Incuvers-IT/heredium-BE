package art.heredium.domain.program.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import art.heredium.domain.program.entity.ProgramPrice;

public interface ProgramPriceRepository extends JpaRepository<ProgramPrice, Long> {
  @Override
  @EntityGraph(attributePaths = "program")
  Optional<ProgramPrice> findById(Long aLong);

  @Override
  @EntityGraph(attributePaths = "program")
  List<ProgramPrice> findAllById(Iterable<Long> longs);
}
