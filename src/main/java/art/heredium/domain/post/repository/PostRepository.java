package art.heredium.domain.post.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import art.heredium.domain.post.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryQueryDsl {
  List<Post> findAllByIsEnabledTrue();

  Optional<Post> findFirstByIsEnabledTrue();

  Optional<Post> findById(long id);

  @Query(
      "SELECT p FROM Membership m LEFT JOIN Post p ON m.post.id = p.id WHERE m.id = :membership_id AND p.isEnabled IS TRUE")
  Optional<Post> findByMembershipIdAndIsEnabledTrue(@Param("membership_id") long membershipId);

  Optional<Post> findFirstByOrderByIdDesc();
}
