package art.heredium.service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.post.entity.Post;
import art.heredium.domain.post.model.dto.request.PostCreateRequest;
import art.heredium.domain.post.model.dto.response.PostResponse;
import art.heredium.domain.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class PostService {
  private final PostRepository postRepository;
  private final MembershipService membershipService;

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

  @Transactional(rollbackFor = Exception.class)
  public void updateIsEnabled(long postId, boolean isEnabled) {
    Post existingPost =
        this.postRepository
            .findById(postId)
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
    if (existingPost.getIsEnabled() == isEnabled) {
      return;
    }
    existingPost.updateIsEnabled(isEnabled);
  }

  @Transactional
  public Long createPost(PostCreateRequest request) {
    final Post post =
        Post.builder()
            .name(request.getName())
            .imageUrl(request.getImageUrl())
            .isEnabled(request.getIsEnabled())
            .contentDetail(request.getContentDetail())
            .navigationLink(request.getNavigationLink())
            .build();
    final Post savedPost = postRepository.save(post);

    if (request.getIsEnabled() && request.getMemberships() != null) {
      membershipService.createMemberships(
          savedPost.getId(), request.getMemberships().getMemberships());
    }

    return savedPost.getId();
  }
}