package art.heredium.domain.account.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import art.heredium.domain.account.entity.AccountInfo;

public interface AccountInfoRepository extends JpaRepository<AccountInfo, Long> {

  @Query(
      "SELECT ai.id FROM AccountInfo ai INNER JOIN Account a ON a.id = ai.id WHERE FUNCTION('DATEDIFF', CURRENT_TIMESTAMP, ai.lastLoginDate) >= :day OR (ai.lastLoginDate IS NULL AND FUNCTION('DATEDIFF', CURRENT_TIMESTAMP, a.createdDate) >= :day)")
  List<Long> findToSleeper(@Param("day") int day);

  @Query(
      "SELECT ai FROM AccountInfo ai INNER JOIN Account a ON a.id = ai.id WHERE FUNCTION('DATEDIFF', CURRENT_TIMESTAMP, ai.lastLoginDate) = :day OR (ai.lastLoginDate IS NULL AND FUNCTION('DATEDIFF', CURRENT_TIMESTAMP, a.createdDate) = :day)")
  List<AccountInfo> findPreToSleeper(@Param("day") int day);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("DELETE FROM AccountInfo ai WHERE ai.id IN (:ids)")
  void deleteAllByIdIn(@Param("ids") Collection<Long> ids);
}
