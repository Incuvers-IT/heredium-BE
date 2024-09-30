package art.heredium.domain.ticket.repository;

import art.heredium.domain.ticket.entity.TicketUuid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketUuidRepository extends JpaRepository<TicketUuid, String> {

    @Override
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TicketUuid t WHERE t.uuid = :id")
    void deleteById(@Param("id") String id);
}