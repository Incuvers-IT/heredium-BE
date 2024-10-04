package art.heredium.domain.membership.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import art.heredium.domain.membership.entity.Membership;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
  @Query("SELECT mr FROM Membership mr WHERE mr.post.id = :post_id AND mr.isEnabled IS TRUE")
  List<Membership> findByPostIdAndIsEnabledTrue(@Param("post_id") long postId);
}
