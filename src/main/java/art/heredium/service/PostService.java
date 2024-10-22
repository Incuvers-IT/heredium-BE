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

import org.apache.commons.lang3.StringUtils;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.AuthUtil;
import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.account.repository.AdminRepository;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.domain.post.entity.Post;
import art.heredium.domain.post.model.dto.request.GetAdminPostRequest;
import art.heredium.domain.post.model.dto.request.PostCreateRequest;
import art.heredium.domain.post.model.dto.response.PostResponse;
import art.heredium.domain.post.repository.PostRepository;
import art.heredium.ncloud.bean.CloudStorage;

@Service
@RequiredArgsConstructor
public class PostService {
  private static final String THUMBNAIL_URL_DELIMITER = ";";
  private final PostRepository postRepository;
  private final MembershipService membershipService;
  private final AdminRepository adminRepository;
  private final CloudStorage cloudStorage;

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
                    post.getImageOriginalFileName(),
                    post.getIsEnabled(),
                    post.getContentDetail(),
                    post.getNavigationLink(),
                    post.getAdmin().getAdminInfo().getName(),
                    post.getCreatedDate(),
                    post.getThumbnailUrls()))
        .collect(Collectors.toList());
  }

  public Optional<Post> findFirstByIsEnabledTrue() {
    return this.postRepository.findFirstByIsEnabledTrue();
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
    Admin admin =
        AuthUtil.getCurrentAdmin().orElseThrow(() -> new ApiException(ErrorCode.ADMIN_NOT_FOUND));
    final PostCreateRequest.AdditionalInfo additionalInfo = request.getAdditionalInfo();

    final Post post =
        Post.builder()
            .name(request.getName())
            .imageUrl(request.getNoteImage().getNoteImageUrl())
            .imageOriginalFileName(request.getNoteImage().getOriginalFileName())
            .isEnabled(request.getIsEnabled())
            .contentDetail(request.getContentDetail())
            .navigationLink(request.getNavigationLink())
            .admin(admin)
            .futureExhibitionCount(
                Optional.ofNullable(additionalInfo)
                    .map(PostCreateRequest.AdditionalInfo::getFutureExhibitionCount)
                    .orElse(null))
            .ongoingExhibitionCount(
                Optional.ofNullable(additionalInfo)
                    .map(PostCreateRequest.AdditionalInfo::getOngoingExhibitionCount)
                    .orElse(null))
            .completedExhibitionCount(
                Optional.ofNullable(additionalInfo)
                    .map(PostCreateRequest.AdditionalInfo::getCompletedExhibitionCount)
                    .orElse(null))
            .futureProgramCount(
                Optional.ofNullable(additionalInfo)
                    .map(PostCreateRequest.AdditionalInfo::getFutureProgramCount)
                    .orElse(null))
            .ongoingProgramCount(
                Optional.ofNullable(additionalInfo)
                    .map(PostCreateRequest.AdditionalInfo::getOngoingProgramCount)
                    .orElse(null))
            .completedProgramCount(
                Optional.ofNullable(additionalInfo)
                    .map(PostCreateRequest.AdditionalInfo::getCompletedProgramCount)
                    .orElse(null))
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .build();
    final Post savedPost = postRepository.saveAndFlush(post);
    final long postId = savedPost.getId();
    final String fileFolderPath = String.format("%s/%d", FilePathType.POST.getPath(), postId);
    final String imageUrl = request.getNoteImage().getNoteImageUrl();
    if (!StringUtils.isEmpty(imageUrl)) {
      validateImage(imageUrl);
      post.updateImageUrl(this.moveImageToNewPlace(imageUrl, fileFolderPath));
    }
    post.updateThumbnailUrls(this.buildThumbnailUrls(request.getThumbnailUrls(), postId));
    post.updateContentDetail(this.moveEditorContent(request.getContentDetail(), fileFolderPath));

    if (request.getMemberships() != null) {
      membershipService.createMemberships(postId, request.getMemberships());
    }

    return postId;
  }

  private String buildThumbnailUrls(
      @Nullable PostCreateRequest.ThumbnailUrl thumbnailUrl, final long postId) {
    if (thumbnailUrl == null) {
      return null;
    }
    final String small =
        Optional.ofNullable(thumbnailUrl.getSmallThumbnailUrl())
            .map(url -> this.moveImageToNewPlace(url, FilePathType.POST.getPath() + "/" + postId))
            .orElse(StringUtils.EMPTY);
    final String medium =
        Optional.ofNullable(thumbnailUrl.getMediumThumbnailUrl())
            .map(url -> this.moveImageToNewPlace(url, FilePathType.POST.getPath() + "/" + postId))
            .orElse(StringUtils.EMPTY);
    final String large =
        Optional.ofNullable(thumbnailUrl.getLargeThumbnailUrl())
            .map(url -> this.moveImageToNewPlace(url, FilePathType.POST.getPath() + "/" + postId))
            .orElse(StringUtils.EMPTY);
    return String.join(THUMBNAIL_URL_DELIMITER, small, medium, large);
  }

  @Transactional(readOnly = true)
  public Page<PostResponse> list(GetAdminPostRequest dto, Pageable pageable) {
    return postRepository.search(dto, pageable);
  }

  public Optional<Post> findFirst() {
    return this.postRepository.findFirstByOrderByIdDesc();
  }

  private void validateImage(String imageUrl) {
    if (imageUrl == null || !cloudStorage.isExistObject(imageUrl)) {
      throw new ApiException(ErrorCode.S3_NOT_FOUND, imageUrl);
    }
  }

  private String moveImageToNewPlace(String tempOriginalUrl, String newPath) {
    Storage storage = new Storage();
    storage.setSavedFileName(tempOriginalUrl);
    Constants.moveFileFromTemp(this.cloudStorage, storage, newPath);
    return storage.getSavedFileName();
  }

  private String moveEditorContent(final String editorContent, final String newPath) {
    final List<String> imageUrls = Constants.getImageNameFromHtml(editorContent);
    String result = editorContent;
    for (String tempImageUrl : imageUrls) {
      validateImage(tempImageUrl);
      final String newImageUrl = this.moveImageToNewPlace(tempImageUrl, newPath);
      result = result.replace(tempImageUrl, newImageUrl);
    }
    return result;
  }
}
