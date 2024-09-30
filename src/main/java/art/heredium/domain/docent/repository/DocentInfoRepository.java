package art.heredium.domain.docent.repository;

import art.heredium.domain.docent.entity.DocentInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocentInfoRepository extends JpaRepository<DocentInfo, Long> {
    Page<DocentInfo> findAllByDocent_IdOrderByOrderAsc(Long docent_id, Pageable pageable);

    @Query("SELECT di FROM DocentInfo di INNER JOIN Docent d ON d.id = di.docent.id WHERE d.id = :id AND d.startDate <= CURRENT_TIMESTAMP AND d.endDate >= CURRENT_TIMESTAMP AND d.isEnabled IS TRUE ORDER BY di.order ASC")
    List<DocentInfo> findPostingInfos(@Param("id") Long id);

    @Query(value = "WITH s AS (SELECT row_number() OVER (ORDER BY di.orders ASC) AS num, di.id " +
            "           FROM docent_info di " +
            "           INNER JOIN docent d ON d.id = di.docent_id" +
            "           WHERE d.id = :docentId AND d.start_date <= now() AND d.end_date >= now() AND d.is_enabled IS TRUE " +
            "           ORDER BY di.orders ASC), " +
            "     s2 AS (SELECT s.*, LAG(s.id) OVER (ORDER BY num ) AS recordId FROM s) " +
            "SELECT di.* FROM docent_info di WHERE di.id = (SELECT s2.recordId FROM s2 WHERE s2.id = :id)", nativeQuery = true)
    DocentInfo findPrev(@Param("docentId") Long docentId, @Param("id") Long id);

    @Query(value = "WITH s AS (SELECT row_number() OVER (ORDER BY di.orders ASC) AS num, di.id " +
            "           FROM docent_info di " +
            "           INNER JOIN docent d ON d.id = di.docent_id" +
            "           WHERE d.id = :docentId AND d.start_date <= now() AND d.end_date >= now() AND d.is_enabled IS TRUE " +
            "           ORDER BY di.orders ASC), " +
            "     s2 AS (SELECT s.*, LEAD(s.id) OVER (ORDER BY num ) AS recordId FROM s) " +
            "SELECT di.* FROM docent_info di WHERE di.id = (SELECT s2.recordId FROM s2 WHERE s2.id = :id)", nativeQuery = true)
    DocentInfo findNext(@Param("docentId") Long docentId, @Param("id") Long id);
}
