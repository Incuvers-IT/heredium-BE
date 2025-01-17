package art.heredium.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import art.heredium.domain.post.entity.PostHistory;

public interface PostHistoryRepository extends JpaRepository<PostHistory, Long> {}
