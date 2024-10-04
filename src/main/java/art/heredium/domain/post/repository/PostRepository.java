package art.heredium.domain.post.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import art.heredium.domain.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
  Optional<Post> findByIdAndIsEnabledTrue(long id);
}
