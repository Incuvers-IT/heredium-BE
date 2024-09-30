package art.heredium.domain.program.repository;

import art.heredium.domain.program.entity.ProgramPrice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgramPriceRepository extends JpaRepository<ProgramPrice, Long> {
    @Override
    @EntityGraph(attributePaths = "program")
    Optional<ProgramPrice> findById(Long aLong);

    @Override
    @EntityGraph(attributePaths = "program")
    List<ProgramPrice> findAllById(Iterable<Long> longs);
}