package art.heredium.domain.exhibition.repository;

import art.heredium.domain.exhibition.entity.ExhibitionRound;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ExhibitionRoundRepository extends JpaRepository<ExhibitionRound, Long> {
    @Override
    @EntityGraph(attributePaths = "exhibition")
    Optional<ExhibitionRound> findById(Long aLong);

    ExhibitionRound findByExhibition_IdAndStartDate(Long exhibition_id, LocalDateTime startDate);
}
