package art.heredium.domain.log.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import art.heredium.domain.log.entity.Log;

public interface LogRepository extends JpaRepository<Log, Long>, LogRepositoryCustom {

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE Log l SET l.admin = null WHERE l.admin.id = :id")
  void setAdminNull(@Param("id") Long id);
}
