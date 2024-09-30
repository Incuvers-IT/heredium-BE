package art.heredium.domain.event.repository;

import art.heredium.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryQueryDsl {
    @Query(value = "WITH s AS (SELECT row_number() OVER (ORDER BY CASE " +
            "                                       WHEN current_date < e.start_date THEN 1 " +
            "                                       WHEN current_date > e.end_date THEN 2 " +
            "                                       ELSE 0 END ASC, " +
            "                                       e.start_date DESC, " +
            "                                       e.end_date DESC) AS num, e.id " +
            "           FROM event e " +
            "           WHERE e.is_enabled IS TRUE " +
            "           ORDER BY CASE " +
            "                        WHEN current_date < e.start_date THEN 1 " +
            "                        WHEN current_date > e.end_date THEN 2 " +
            "                        ELSE 0 " +
            "                        END ASC, " +
            "                    e.start_date DESC, " +
            "                    e.end_date DESC), " +
            "     s2 AS (SELECT s.*, LEAD(s.id) OVER (ORDER BY num ) AS prev FROM s) " +
            "SELECT e.* FROM event e WHERE e.id = (SELECT s2.prev FROM s2 WHERE s2.id = :id)", nativeQuery = true)
    Event findPrev(@Param("id") Long id);

    @Query(value = "WITH s AS (SELECT row_number() OVER (ORDER BY CASE " +
            "                                       WHEN current_date < e.start_date THEN 1 " +
            "                                       WHEN current_date > e.end_date THEN 2 " +
            "                                       ELSE 0 END ASC, " +
            "                                       e.start_date DESC, " +
            "                                       e.end_date DESC) AS num, e.id " +
            "           FROM event e " +
            "           WHERE e.is_enabled IS TRUE " +
            "           ORDER BY CASE " +
            "                        WHEN current_date < e.start_date THEN 1 " +
            "                        WHEN current_date > e.end_date THEN 2 " +
            "                        ELSE 0 " +
            "                        END ASC, " +
            "                    e.start_date DESC, " +
            "                    e.end_date DESC), " +
            "     s2 AS (SELECT s.*, LAG(s.id) OVER (ORDER BY num ) AS next FROM s) " +
            "SELECT e.* FROM event e WHERE e.id = (SELECT s2.next FROM s2 WHERE s2.id = :id)", nativeQuery = true)
    Event findNext(@Param("id") Long id);
}
