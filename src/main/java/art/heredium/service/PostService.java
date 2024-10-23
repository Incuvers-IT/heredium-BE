package art.heredium.service;

import java.util.ArrayList;
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
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.model.dto.request.MembershipCouponCreateRequest;
import art.heredium.domain.coupon.repository.CouponRepository;
import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.model.dto.request.MembershipCreateRequest;
import art.heredium.domain.membership.repository.MembershipRepository;
import art.heredium.domain.post.entity.Post;
import art.heredium.domain.post.model.dto.request.GetAdminPostRequest;
import art.heredium.domain.post.model.dto.request.MembershipCouponUpdateRequest;
import art.heredium.domain.post.model.dto.request.PostCreateRequest;
import art.heredium.domain.post.model.dto.request.PostMembershipUpdateRequest;
import art.heredium.domain.post.model.dto.request.PostUpdateRequest;
import art.heredium.domain.post.model.dto.response.PostResponse;
import art.heredium.domain.post.repository.PostRepository;
import art.heredium.ncloud.bean.CloudStorage;

@Service
@RequiredArgsConstructor
public class PostService {
  private static final String THUMBNAIL_URL_DELIMITER = ";";
  private final PostRepository postRepository;
  private final MembershipService membershipService;
  private final CloudStorage cloudStorage;
  private final MembershipRepository membershipRepository;
  private final CouponRepository couponRepository;

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

  @Transactional
  public void updatePost(PostUpdateRequest request) {
    Post post =
        postRepository
            .findFirstByOrderByIdDesc()
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

    updatePostFields(post, request);
    updateMemberships(post, request.getMemberships());

    postRepository.save(post);
  }

  private void updatePostFields(Post post, PostUpdateRequest request) {
    if (request.getName() != null) post.setName(request.getName());
    if (request.getIsEnabled() != null) post.setIsEnabled(request.getIsEnabled());
    if (request.getNavigationLink() != null) post.setNavigationLink(request.getNavigationLink());
    if (request.getContentDetail() != null) post.setContentDetail(request.getContentDetail());
    if (request.getThumbnailUrls() != null) updateThumbnailUrls(post, request.getThumbnailUrls());
    if (request.getNoteImage() != null) updateNoteImage(post, request.getNoteImage());
    if (request.getAdditionalInfo() != null)
      updateAdditionalInfo(post, request.getAdditionalInfo());
    if (request.getStartDate() != null) post.setStartDate(request.getStartDate());
    if (request.getEndDate() != null) post.setEndDate(request.getEndDate());
  }

  private void updateThumbnailUrls(Post post, PostUpdateRequest.ThumbnailUrl thumbnailUrls) {
    if (thumbnailUrls == null) return;

    String fileFolderPath = String.format("%s/%d", FilePathType.POST.getPath(), post.getId());

    String[] existingUrls = post.getThumbnailUrls().split(THUMBNAIL_URL_DELIMITER);
    String existingSmall = existingUrls.length > 0 ? existingUrls[0] : "";
    String existingMedium = existingUrls.length > 1 ? existingUrls[1] : "";
    String existingLarge = existingUrls.length > 2 ? existingUrls[2] : "";

    String small =
        updateThumbnailUrl(thumbnailUrls.getSmallThumbnailUrl(), existingSmall, fileFolderPath);
    String medium =
        updateThumbnailUrl(thumbnailUrls.getMediumThumbnailUrl(), existingMedium, fileFolderPath);
    String large =
        updateThumbnailUrl(thumbnailUrls.getLargeThumbnailUrl(), existingLarge, fileFolderPath);

    String updatedThumbnailUrls = String.join(THUMBNAIL_URL_DELIMITER, small, medium, large);
    post.setThumbnailUrls(updatedThumbnailUrls);
  }

  private String updateThumbnailUrl(String newUrl, String existingUrl, String fileFolderPath) {
    if (StringUtils.isEmpty(newUrl)) {
      return existingUrl;
    }

    validateImage(newUrl);
    return moveImageToNewPlace(newUrl, fileFolderPath);
  }

  private void updateNoteImage(Post post, PostUpdateRequest.NoteImage noteImage) {
    if (noteImage == null) return;

    if (noteImage.getOriginalFileName() != null) {
      post.setImageOriginalFileName(noteImage.getOriginalFileName());
    }

    String noteImageUrl = noteImage.getNoteImageUrl();
    if (!StringUtils.isEmpty(noteImageUrl)) {
      String fileFolderPath = String.format("%s/%d", FilePathType.POST.getPath(), post.getId());
      validateImage(noteImageUrl);
      String newImageUrl = this.moveImageToNewPlace(noteImageUrl, fileFolderPath);
      post.setImageUrl(newImageUrl);
    }
  }

  private void updateAdditionalInfo(Post post, PostUpdateRequest.AdditionalInfo additionalInfo) {
    if (additionalInfo == null) return;

    if (additionalInfo.getFutureExhibitionCount() != null)
      post.setFutureExhibitionCount(additionalInfo.getFutureExhibitionCount());
    if (additionalInfo.getOngoingExhibitionCount() != null)
      post.setOngoingExhibitionCount(additionalInfo.getOngoingExhibitionCount());
    if (additionalInfo.getCompletedExhibitionCount() != null)
      post.setCompletedExhibitionCount(additionalInfo.getCompletedExhibitionCount());
    if (additionalInfo.getFutureProgramCount() != null)
      post.setFutureProgramCount(additionalInfo.getFutureProgramCount());
    if (additionalInfo.getOngoingProgramCount() != null)
      post.setOngoingProgramCount(additionalInfo.getOngoingProgramCount());
    if (additionalInfo.getCompletedProgramCount() != null)
      post.setCompletedProgramCount(additionalInfo.getCompletedProgramCount());
  }

  private void updateMemberships(Post post, List<PostMembershipUpdateRequest> membershipRequests) {
    if (membershipRequests == null) return;

    for (PostMembershipUpdateRequest membershipRequest : membershipRequests) {
      if (membershipRequest.getId() != null) {
        Membership membership =
            post.getMemberships().stream()
                .filter(m -> m.getId().equals(membershipRequest.getId()))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBERSHIP_NOT_FOUND));
        updateMembership(membership, membershipRequest);
      } else {
        createNewMembership(post, membershipRequest);
      }
    }
  }

  private void updateMembership(Membership membership, PostMembershipUpdateRequest request) {
    if (request.getName() != null) membership.setName(request.getName());
    if (request.getPrice() != null) membership.setPrice(request.getPrice());
    if (request.getImageUrl() != null) {
      membershipService.validateImage(request.getImageUrl());
      String newMembershipPath = FilePathType.MEMBERSHIP.getPath() + "/" + membership.getId();
      String permanentImageUrl =
          membershipService.moveImageToNewPlace(request.getImageUrl(), newMembershipPath);
      membership.setImageUrl(permanentImageUrl);
    }
    if (request.getPeriod() != null) membership.setPeriod(request.getPeriod());

    membershipRepository.save(membership);

    updateCoupons(membership, request.getCoupons());
  }

  private void createNewMembership(Post post, PostMembershipUpdateRequest request) {
    MembershipCreateRequest createRequest = new MembershipCreateRequest();
    createRequest.setName(request.getName());
    createRequest.setPrice(request.getPrice());
    createRequest.setImageUrl(request.getImageUrl());
    createRequest.setCoupons(convertToMembershipCouponCreateRequests(request.getCoupons()));

    List<MembershipCreateRequest> createRequests = List.of(createRequest);
    membershipService.createMemberships(post.getId(), createRequests);
  }

  private List<MembershipCouponCreateRequest> convertToMembershipCouponCreateRequests(
      List<MembershipCouponUpdateRequest> updateRequests) {
    if (updateRequests == null) return new ArrayList<>();

    return updateRequests.stream()
        .map(this::convertToMembershipCouponCreateRequest)
        .collect(Collectors.toList());
  }

  private MembershipCouponCreateRequest convertToMembershipCouponCreateRequest(
      MembershipCouponUpdateRequest updateRequest) {
    MembershipCouponCreateRequest createRequest = new MembershipCouponCreateRequest();
    createRequest.setName(updateRequest.getName());
    createRequest.setCouponType(updateRequest.getCouponType());
    createRequest.setDiscountPercent(updateRequest.getDiscountPercent());
    createRequest.setPeriodInDays(updateRequest.getPeriodInDays());
    createRequest.setImageUrl(updateRequest.getImageUrl());
    createRequest.setNumberOfUses(updateRequest.getNumberOfUses());
    createRequest.setIsPermanent(updateRequest.getIsPermanent());
    return createRequest;
  }

  private void updateCoupons(
      Membership membership, List<MembershipCouponUpdateRequest> couponRequests) {
    if (couponRequests == null) return;

    for (MembershipCouponUpdateRequest couponRequest : couponRequests) {
      if (couponRequest.getId() != null) {
        Coupon coupon =
            membership.getCoupons().stream()
                .filter(c -> c.getId().equals(couponRequest.getId()))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.COUPON_NOT_FOUND));
        updateCoupon(coupon, couponRequest);
      } else {
        createNewCoupon(membership, couponRequest);
      }
    }
  }

  private void updateCoupon(Coupon coupon, MembershipCouponUpdateRequest request) {
    if (request.getName() != null) coupon.setName(request.getName());
    if (request.getCouponType() != null) coupon.setCouponType(request.getCouponType());
    if (request.getDiscountPercent() != null)
      coupon.setDiscountPercent(request.getDiscountPercent());
    if (request.getPeriodInDays() != null) coupon.setPeriodInDays(request.getPeriodInDays());
    if (request.getImageUrl() != null) {
      membershipService.validateImage(request.getImageUrl());
      String newCouponPath = FilePathType.COUPON.getPath() + "/" + coupon.getId();
      String permanentCouponImageUrl =
          membershipService.moveImageToNewPlace(request.getImageUrl(), newCouponPath);
      coupon.setImageUrl(permanentCouponImageUrl);
    }
    if (request.getNumberOfUses() != null) coupon.setNumberOfUses(request.getNumberOfUses());
    if (request.getIsPermanent() != null) coupon.setIsPermanent(request.getIsPermanent());

    couponRepository.save(coupon);
  }

  private void createNewCoupon(Membership membership, MembershipCouponUpdateRequest request) {
    MembershipCouponCreateRequest createRequest = convertToMembershipCouponCreateRequest(request);

    membershipService.validateCouponRequest(createRequest);
    membershipService.validateImage(createRequest.getImageUrl());

    Coupon newCoupon =
        Coupon.builder()
            .name(createRequest.getName())
            .couponType(createRequest.getCouponType())
            .discountPercent(createRequest.getDiscountPercent())
            .periodInDays(createRequest.getPeriodInDays())
            .imageUrl(createRequest.getImageUrl())
            .membership(membership)
            .numberOfUses(createRequest.getNumberOfUses())
            .isPermanent(createRequest.getIsPermanent())
            .build();

    Coupon savedCoupon = couponRepository.save(newCoupon);

    if (StringUtils.isNotEmpty(createRequest.getImageUrl())) {
      String newCouponPath = FilePathType.COUPON.getPath() + "/" + savedCoupon.getId();
      String permanentCouponImageUrl =
          membershipService.moveImageToNewPlace(createRequest.getImageUrl(), newCouponPath);
      savedCoupon.setImageUrl(permanentCouponImageUrl);
      couponRepository.save(savedCoupon);
    }
  }
}
