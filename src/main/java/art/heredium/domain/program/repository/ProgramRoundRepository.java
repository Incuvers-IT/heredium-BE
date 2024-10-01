package art.heredium.domain.program.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import art.heredium.domain.program.entity.ProgramRound;

public interface ProgramRoundRepository extends JpaRepository<ProgramRound, Long> {
  @Override
  @EntityGraph(attributePaths = "program")
  Optional<ProgramRound> findById(Long aLong);
}
