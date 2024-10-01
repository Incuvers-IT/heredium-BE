package art.heredium.domain.account.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import art.heredium.domain.account.entity.SleeperInfo;

public interface SleeperInfoRepository extends JpaRepository<SleeperInfo, Long> {

  @Query(
      "SELECT si FROM SleeperInfo si WHERE FUNCTION('DATEDIFF', CURRENT_TIMESTAMP, si.sleepDate) = :day")
  List<SleeperInfo> findPreToTerminate(@Param("day") int day);

  @Override
  @EntityGraph(attributePaths = "account")
  Optional<SleeperInfo> findById(Long aLong);
}
