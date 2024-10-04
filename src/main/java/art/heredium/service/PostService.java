package art.heredium.service;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import art.heredium.domain.post.entity.Post;
import art.heredium.domain.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class PostService {
  private final PostRepository postRepository;

  public Optional<Post> findByIdAndIsEnabledTrue(long postId) {
    return this.postRepository.findByIdAndIsEnabledTrue(postId);
  }
}
