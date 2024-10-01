package art.heredium.domain.policy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import art.heredium.domain.policy.entity.Policy;

public interface PolicyRepository extends JpaRepository<Policy, Long>, PolicyRepositoryQueryDsl {

  @Query(
      value =
          "SELECT * FROM policy p WHERE now() > p.post_date AND p.type = :type AND p.is_enabled IS TRUE ORDER BY p.post_date DESC, p.last_modified_date DESC limit 1",
      nativeQuery = true)
  Policy findPosting(@Param("type") Integer type);

  @Query(
      value =
          "SELECT * FROM policy p WHERE now() > p.post_date AND p.type = :type AND p.is_enabled IS TRUE ORDER BY p.post_date DESC, p.last_modified_date DESC",
      nativeQuery = true)
  List<Policy> listByUser(@Param("type") Integer type);
}
