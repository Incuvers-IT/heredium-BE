package art.heredium.domain.docent.repository;

import art.heredium.domain.docent.entity.Docent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocentRepository extends JpaRepository<Docent, Long>, DocentRepositoryQueryDsl {
    @Query("SELECT d FROM Docent d WHERE d.startDate <= CURRENT_TIMESTAMP AND d.endDate >= CURRENT_TIMESTAMP AND d.isEnabled IS TRUE ORDER BY d.startDate DESC, d.endDate DESC, d.lastModifiedDate DESC")
    List<Docent> findPostingByUser();

    @Query("SELECT d FROM Docent d WHERE d.id = :id AND d.startDate <= CURRENT_TIMESTAMP AND d.endDate >= CURRENT_TIMESTAMP AND d.isEnabled IS TRUE ORDER BY d.startDate DESC, d.endDate DESC, d.lastModifiedDate DESC")
    Docent findPostingByUserAndId(@Param("id") Long id);
}
