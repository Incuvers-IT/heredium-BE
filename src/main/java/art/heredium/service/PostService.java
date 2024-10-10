package art.heredium.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.account.repository.AdminRepository;
import art.heredium.domain.post.entity.Post;
import art.heredium.domain.post.model.dto.request.GetAdminPostRequest;
import art.heredium.domain.post.model.dto.request.PostCreateRequest;
import art.heredium.domain.post.model.dto.response.PostResponse;
import art.heredium.domain.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class PostService {
  private static final String THUMBNAIL_URL_DELIMITER = ";";
  private final PostRepository postRepository;
  private final MembershipService membershipService;
  private final AdminRepository adminRepository;

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
                    post.getNavigationLink(),
                    post.getAdmin().getAdminInfo().getName(),
                    post.getCreatedDate(),
                    post.getThumbnailUrls()))
        .collect(Collectors.toList());
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
  public Long createPost(PostCreateRequest request, Long adminId) {
    Admin admin =
        adminRepository
            .findById(adminId)
            .orElseThrow(() -> new ApiException(ErrorCode.ADMIN_NOT_FOUND));

    final String thumbnailUrls = this.buildThumbnailUrls(request.getThumbnailUrl());
    final Post post =
        Post.builder()
            .name(request.getName())
            .imageUrl(request.getImageUrl())
            .thumbnailUrls(thumbnailUrls)
            .isEnabled(request.getIsEnabled())
            .contentDetail(request.getContentDetail())
            .navigationLink(request.getNavigationLink())
            .admin(admin)
            .build();
    final Post savedPost = postRepository.save(post);

    if (request.getIsEnabled() && request.getMemberships() != null) {
      membershipService.createMemberships(
          savedPost.getId(), request.getMemberships().getMemberships());
    }

    return savedPost.getId();
  }

  private String buildThumbnailUrls(@Nullable PostCreateRequest.ThumbnailUrl thumbnailUrl) {
    if (thumbnailUrl == null) return null;
    return thumbnailUrl.getSmallThumbnailUrl()
        + THUMBNAIL_URL_DELIMITER
        + thumbnailUrl.getMediumThumbnailUrl()
        + THUMBNAIL_URL_DELIMITER
        + thumbnailUrl.getLargeThumbnailUrl();
  }

  @Transactional(readOnly = true)
  public Page<PostResponse> list(GetAdminPostRequest dto, Pageable pageable) {
    return postRepository.search(dto, pageable);
  }
}
