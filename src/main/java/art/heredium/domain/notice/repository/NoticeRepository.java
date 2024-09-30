package art.heredium.domain.notice.repository;

import art.heredium.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<Notice, Long>, NoticeRepositoryQueryDsl {
    @Query(value = "WITH s AS (SELECT row_number() OVER (ORDER BY e.is_notice DESC, e.post_date DESC, e.last_modified_date DESC) AS num, e.id " +
            "           FROM notice e " +
            "           WHERE e.is_enabled IS TRUE " +
            "           ORDER BY e.is_notice DESC, e.post_date DESC, e.last_modified_date DESC), " +
            "     s2 AS (SELECT s.*, LEAD(s.id) OVER (ORDER BY num ) AS prev FROM s) " +
            "SELECT e.* FROM notice e WHERE e.id = (SELECT s2.prev FROM s2 WHERE s2.id = :id)", nativeQuery = true)
    Notice findPrev(@Param("id") Long id);

    @Query(value = "WITH s AS (SELECT row_number() OVER (ORDER BY e.is_notice DESC, e.post_date DESC, e.last_modified_date DESC) AS num, e.id " +
            "           FROM notice e " +
            "           WHERE e.is_enabled IS TRUE " +
            "           ORDER BY e.is_notice DESC, e.post_date DESC, e.last_modified_date DESC), " +
            "     s2 AS (SELECT s.*, LAG(s.id) OVER (ORDER BY num ) AS next FROM s) " +
            "SELECT e.* FROM notice e WHERE e.id = (SELECT s2.next FROM s2 WHERE s2.id = :id)", nativeQuery = true)
    Notice findNext(@Param("id") Long id);
}
