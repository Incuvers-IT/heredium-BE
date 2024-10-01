package art.heredium.domain.account.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import art.heredium.domain.account.entity.NonUser;

public interface NonUserRepository extends JpaRepository<NonUser, Long>, NonUserRepositoryQueryDsl {

  Optional<NonUser> findByPhoneAndHanaBankUuidIsNull(String phone);

  Optional<NonUser> findByHanaBankUuid(String hanaBankUuid);

  @Query(
      "SELECT a FROM NonUser a WHERE a.name IS NOT NULL AND FUNCTION('DATEDIFF', CURRENT_TIMESTAMP, a.lastModifiedDate) >= :day")
  List<NonUser> findToTerminate(@Param("day") int day);
}
