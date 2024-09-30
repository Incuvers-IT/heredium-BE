package art.heredium.domain.ticket.repository;

import art.heredium.domain.ticket.entity.TicketLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketLogRepository extends JpaRepository<TicketLog, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TicketLog l SET l.admin = null WHERE l.admin.id = :id")
    void setAdminNull(@Param("id") Long id);
}
