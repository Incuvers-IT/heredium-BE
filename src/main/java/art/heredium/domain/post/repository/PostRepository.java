package art.heredium.domain.post.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import art.heredium.domain.post.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
  List<Post> findAllByIsEnabledTrue();
  Optional<Post> findByIdAndIsEnabledTrue(long id);
}
