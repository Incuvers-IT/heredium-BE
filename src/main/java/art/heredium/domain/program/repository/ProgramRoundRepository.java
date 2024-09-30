package art.heredium.domain.program.repository;

import art.heredium.domain.program.entity.ProgramRound;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgramRoundRepository extends JpaRepository<ProgramRound, Long> {
    @Override
    @EntityGraph(attributePaths = "program")
    Optional<ProgramRound> findById(Long aLong);
}
