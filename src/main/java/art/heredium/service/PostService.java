package art.heredium.service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.domain.post.entity.Post;
import art.heredium.domain.post.model.dto.response.PostResponse;
import art.heredium.domain.post.repository.PostRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
  private final PostRepository postRepository;

  @Transactional(readOnly = true)
  public List<PostResponse> getEnabledPosts() {
    final List<Post> posts = postRepository.findAllByIsEnabledTrue();
    return posts.stream()
        .map(
            post ->
                new PostResponse(
                    post.getId(),
                    post.getName(),
                    post.getImageUrl(),
                    post.getIsEnabled(),
                    post.getContentDetail(),
                    post.getNavigationLink()))
        .toList();
  }

    public Optional<Post> findByIdAndIsEnabledTrue(long postId) {
        return this.postRepository.findByIdAndIsEnabledTrue(postId);
    }
}
