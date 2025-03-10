package art.heredium.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import art.heredium.domain.company.entity.Company;
import art.heredium.domain.coupon.model.dto.request.CompanyCouponCreateRequest;
import art.heredium.domain.coupon.model.dto.request.CouponCreateRequest;
import art.heredium.domain.coupon.model.dto.request.NonMembershipCouponCreateRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.AuthUtil;
import art.heredium.core.util.Constants;
import art.heredium.core.util.ValidationUtil;
import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.entity.CouponSource;
import art.heredium.domain.coupon.model.dto.request.MembershipCouponCreateRequest;
import art.heredium.domain.coupon.repository.CouponRepository;
import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.model.dto.request.MembershipCreateRequest;
import art.heredium.domain.membership.repository.MembershipRepository;
import art.heredium.domain.post.entity.Post;
import art.heredium.domain.post.entity.PostHistory;
import art.heredium.domain.post.model.dto.request.*;
import art.heredium.domain.post.model.dto.response.AdminPostDetailsResponse;
import art.heredium.domain.post.repository.PostRepository;
import art.heredium.ncloud.bean.CloudStorage;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private static final Long DEFAULT_MEMBERSHIP_PERIOD = 364L; // days
  private static final String THUMBNAIL_URL_DELIMITER = ";";
  private final PostRepository postRepository;
  private final MembershipService membershipService;
  private final PostHistoryService postHistoryService;
  private final CloudStorage cloudStorage;
  private final MembershipRepository membershipRepository;
  private final CouponRepository couponRepository;
  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  public Optional<Post> findFirstByIsEnabledTrue() {
    return this.postRepository.findFirstByIsEnabledTrue();
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
            .subTitle(request.getSubTitle())
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
            .openDate(request.getOpenDate())
            .build();
    final Post savedPost = postRepository.saveAndFlush(post);
    final long postId = savedPost.getId();
    final String fileFolderPath = String.format("%s/%d", FilePathType.POST.getPath(), postId);
    final String imageUrl = request.getNoteImage().getNoteImageUrl();
    if (!StringUtils.isEmpty(imageUrl)) {
      ValidationUtil.validateImage(this.cloudStorage, imageUrl);
      post.updateImageUrl(
          Constants.moveImageToNewPlace(this.cloudStorage, imageUrl, fileFolderPath));
    }
    post.updateThumbnailUrls(this.buildThumbnailUrls(request.getThumbnailUrls(), postId));
    post.updateContentDetail(this.moveEditorContent(request.getContentDetail(), fileFolderPath));

    if (request.getMemberships() != null) {
      membershipService.createMemberships(postId, request.getMemberships());
    }
    this.updatePostHistory(post);

    return postId;
  }

  private String buildThumbnailUrls(
      @Nullable PostCreateRequest.ThumbnailUrl thumbnailUrl, final long postId) {
    if (thumbnailUrl == null) {
      return null;
    }
    final String small =
        Optional.ofNullable(thumbnailUrl.getSmallThumbnailUrl())
            .map(
                url ->
                    Constants.moveImageToNewPlace(
                        this.cloudStorage, url, FilePathType.POST.getPath() + "/" + postId))
            .orElse(StringUtils.EMPTY);
    final String medium =
        Optional.ofNullable(thumbnailUrl.getMediumThumbnailUrl())
            .map(
                url ->
                    Constants.moveImageToNewPlace(
                        this.cloudStorage, url, FilePathType.POST.getPath() + "/" + postId))
            .orElse(StringUtils.EMPTY);
    final String large =
        Optional.ofNullable(thumbnailUrl.getLargeThumbnailUrl())
            .map(
                url ->
                    Constants.moveImageToNewPlace(
                        this.cloudStorage, url, FilePathType.POST.getPath() + "/" + postId))
            .orElse(StringUtils.EMPTY);
    return String.join(THUMBNAIL_URL_DELIMITER, small, medium, large);
  }

  public Optional<Post> findFirst() {
    return this.postRepository.findFirstByOrderByIdDesc();
  }

  private String moveEditorContent(final String editorContent, final String newPath) {
    if (StringUtils.isEmpty(editorContent)) {
      return editorContent;
    }

    final List<String> imageUrls = Constants.getImageNameFromHtml(editorContent);
    String result = editorContent;
    for (String tempImageUrl : imageUrls) {
      ValidationUtil.validateImage(this.cloudStorage, tempImageUrl);
      final String newImageUrl =
          Constants.moveImageToNewPlace(this.cloudStorage, tempImageUrl, newPath);
      result = result.replace(tempImageUrl, newImageUrl);
    }
    return result;
  }

  @Transactional
  public void updatePost(PostUpdateRequest request) {
      try {
          log.info("Request to update post: {}", objectMapper.writeValueAsString(request));
      } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
      }
      Post post =
        postRepository
            .findFirstByOrderByIdDesc()
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

    updatePostFields(post, request);
    updateMemberships(post, request.getMemberships());

    final Post savedPost = postRepository.save(post);
    this.updatePostHistory(savedPost);
  }

  private void updatePostFields(Post post, PostUpdateRequest request) {
    if (request.getName() != null) post.setName(request.getName());
    if (request.getIsEnabled() != null) post.setIsEnabled(request.getIsEnabled());
    if (request.getContentDetail() != null) {
      String fileFolderPath = String.format("%s/%d", FilePathType.POST.getPath(), post.getId());
      String processedContent = moveEditorContent(request.getContentDetail(), fileFolderPath);
      post.setContentDetail(processedContent);
    }
    if (request.getSubTitle() != null) post.setSubTitle(request.getSubTitle());
    if (request.getThumbnailUrls() != null) updateThumbnailUrls(post, request.getThumbnailUrls());
    if (request.getNoteImage() != null) updateNoteImage(post, request.getNoteImage());
    if (request.getAdditionalInfo() != null)
      updateAdditionalInfo(post, request.getAdditionalInfo());
    if (request.getStartDate() != null) post.setStartDate(request.getStartDate());
    if (request.getEndDate() != null) post.setEndDate(request.getEndDate());
    if (request.getOpenDate() != null) post.setOpenDate(request.getOpenDate());
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

    ValidationUtil.validateImage(this.cloudStorage, newUrl);
    return Constants.moveImageToNewPlace(this.cloudStorage, newUrl, fileFolderPath);
  }

  private void updateNoteImage(Post post, PostUpdateRequest.NoteImage noteImage) {
    if (noteImage == null) return;

    if (noteImage.getOriginalFileName() != null) {
      post.setImageOriginalFileName(noteImage.getOriginalFileName());
    }

    String noteImageUrl = noteImage.getNoteImageUrl();
    if (noteImageUrl.equals("")) {
      post.setImageUrl(null);
    }
    if (!StringUtils.isEmpty(noteImageUrl)) {
      String fileFolderPath = String.format("%s/%d", FilePathType.POST.getPath(), post.getId());
      ValidationUtil.validateImage(this.cloudStorage, noteImageUrl);
      String newImageUrl =
          Constants.moveImageToNewPlace(this.cloudStorage, noteImageUrl, fileFolderPath);
      post.setImageUrl(newImageUrl);
    }
  }

  private void updateAdditionalInfo(Post post, PostUpdateRequest.AdditionalInfo additionalInfo) {
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

  public void updateMemberships(Post post, List<PostMembershipUpdateRequest> membershipRequests) {
    if (membershipRequests == null) return;

    for (PostMembershipUpdateRequest membershipRequest : membershipRequests) {
      if (membershipRequest.getId() != null) {
        Membership membership =
            post.getMemberships().stream()
                .filter(m -> m.getId().equals(membershipRequest.getId()))
                .findFirst()
                .orElseThrow(
                    () ->
                        new ApiException(
                            ErrorCode.MEMBERSHIP_NOT_FOUND,
                            "membershipId = " + membershipRequest.getId()));
        updateMembership(membership, membershipRequest);
      } else {
          MembershipCreateRequest createRequest = new MembershipCreateRequest();
          createRequest.setName(membershipRequest.getName());
          createRequest.setPrice(membershipRequest.getPrice());
          createRequest.setImageUrl(membershipRequest.getImageUrl());
          createRequest.setIsEnabled(membershipRequest.getIsEnabled() == null || membershipRequest.getIsEnabled());
          createRequest.setCoupons(convertToCouponCreateRequests(membershipRequest.getCoupons()));
          createRequest.setIsRegisterMembershipButtonShown(
                  membershipRequest.getIsRegisterMembershipButtonShown() == null
                          || membershipRequest.getIsRegisterMembershipButtonShown());

          this.createMemberships(post.getId(), Arrays.asList(createRequest));
      }
    }
  }

  public void createMemberships(long postId, List<MembershipCreateRequest> membershipRequests) {
      final Post post =
              postRepository
                      .findById(postId)
                      .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

      if (!post.getIsEnabled()) {
          throw new ApiException(
                  ErrorCode.POST_NOT_ALLOW, String.format("Post number '%d' is disable", postId));
      }

      for (MembershipCreateRequest request : membershipRequests) {
          // Validate membership image
          ValidationUtil.validateImage(this.cloudStorage, request.getImageUrl());

          Membership membership =
                  Membership.builder()
                          .name(request.getName())
                          .period(DEFAULT_MEMBERSHIP_PERIOD)
                          .price(request.getPrice())
                          .isEnabled(request.getIsEnabled() == null || request.getIsEnabled())
                          .imageUrl(request.getImageUrl())
                          .post(post)
                          .isRegisterMembershipButtonShown(
                                  request.getIsRegisterMembershipButtonShown() == null
                                          || request.getIsRegisterMembershipButtonShown())
                          .build();

          Membership savedMembership = membershipRepository.save(membership);

          if (StringUtils.isNotEmpty(request.getImageUrl())) {
              // Move membership image to permanent storage and update the imageUrl
              String newMembershipPath =
                      FilePathType.MEMBERSHIP.getPath() + "/" + savedMembership.getId();
              String permanentImageUrl =
                      Constants.moveImageToNewPlace(
                              this.cloudStorage, request.getImageUrl(), newMembershipPath);
              savedMembership.updateImageUrl(permanentImageUrl);
              membershipRepository.save(savedMembership);
          }
          request
                  .getCoupons()
                  .forEach(
                          couponRequest ->
                                  this.createMembershipCoupon(couponRequest, savedMembership));
      }
  }

    public void createMembershipCoupon(
            @NonNull final MembershipCouponCreateRequest request,
            @Nullable final Membership membership) {
        if (membership == null) {
            // This case will never happen
            throw new ApiException(
                    ErrorCode.BAD_VALID,
                    String.format(
                            "Invalid coupon request for '%s': If 'isMembershipCoupon' is true, 'membership' must be provided. If 'isMembershipCoupon' is false, 'membership' must not be provided.",
                            request.getName()));
        }
        Integer periodInDays = request.getPeriodInDays();

        ValidationUtil.validateImage(this.cloudStorage, request.getImageUrl());
        final long numberOfUses = request.getIsPermanent() ? 0 : request.getNumberOfUses();

        Coupon coupon =
                Coupon.builder()
                        .name(request.getName())
                        .couponType(request.getCouponType())
                        .discountPercent(request.getDiscountPercent())
                        .startedDate(null)
                        .endedDate(null)
                        .periodInDays(periodInDays)
                        .imageUrl(request.getImageUrl())
                        .membership(membership)
                        .company(null)
                        .numberOfUses(numberOfUses)
                        .isPermanent(request.getIsPermanent())
                        .fromSource(CouponSource.MEMBERSHIP_PACKAGE)
                        .build();

        Coupon savedCoupon = couponRepository.save(coupon);

        if (StringUtils.isNotEmpty(request.getImageUrl())) {
            // Move coupon image to permanent storage and update the imageUrl
            String newCouponPath = FilePathType.COUPON.getPath() + "/" + savedCoupon.getId();
            String permanentCouponImageUrl =
                    Constants.moveImageToNewPlace(this.cloudStorage, request.getImageUrl(), newCouponPath);
            savedCoupon.updateImageUrl(permanentCouponImageUrl);
            couponRepository.save(savedCoupon);
        }
    }

  private void updateMembership(Membership membership, PostMembershipUpdateRequest request) {
    if (Boolean.TRUE.equals(request.getIsDeleted())) {
      membership.setIsDeleted(true);
      membership.setIsEnabled(false);
      membership.setIsRegisterMembershipButtonShown(false);
      membershipRepository.save(membership);
      return;
    }

    if (request.getName() != null) membership.setName(request.getName());
    if (request.getPrice() != null) membership.setPrice(request.getPrice());
    if (request.getImageUrl() != null) {
      ValidationUtil.validateImage(this.cloudStorage, request.getImageUrl());
      String newMembershipPath = FilePathType.MEMBERSHIP.getPath() + "/" + membership.getId();
      String permanentImageUrl =
          Constants.moveImageToNewPlace(
              this.cloudStorage, request.getImageUrl(), newMembershipPath);
      membership.setImageUrl(permanentImageUrl);
    }
    if (request.getPeriod() != null) membership.setPeriod(request.getPeriod());
    if (request.getIsEnabled() != null) membership.setIsEnabled(request.getIsEnabled());
    if (request.getIsRegisterMembershipButtonShown() != null)
      membership.setIsRegisterMembershipButtonShown(request.getIsRegisterMembershipButtonShown());

    membershipRepository.save(membership);

    updateCoupons(membership, request.getCoupons());
  }

  private void updatePostHistory(Post post) {
    String content = null;
    try {
      content = this.objectMapper.writeValueAsString(new AdminPostDetailsResponse(post));
    } catch (JsonProcessingException e) {
      log.info("Failed to deserialize AdminPostDetailsResponse");
    }
    PostHistory savedPostHistory =
        this.postHistoryService.save(
            PostHistory.builder()
                .modifyUserEmail(post.getAdmin().getEmail())
                .postContent(content)
                .build());
    savedPostHistory.updateLastModifiedDate();
    savedPostHistory.updateLastModifiedName();
  }

  private List<MembershipCouponCreateRequest> convertToCouponCreateRequests(
      List<MembershipCouponUpdateRequest> updateRequests) {
    if (updateRequests == null) return new ArrayList<>();

    return updateRequests.stream()
        .map(this::convertToCouponCreateRequest)
        .collect(Collectors.toList());
  }

  private MembershipCouponCreateRequest convertToCouponCreateRequest(
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
                .orElseThrow(
                    () ->
                        new ApiException(
                            ErrorCode.COUPON_NOT_FOUND, "couponId = " + couponRequest.getId()));
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
      ValidationUtil.validateImage(this.cloudStorage, request.getImageUrl());
      String newCouponPath = FilePathType.COUPON.getPath() + "/" + coupon.getId();
      String permanentCouponImageUrl =
          Constants.moveImageToNewPlace(this.cloudStorage, request.getImageUrl(), newCouponPath);
      coupon.setImageUrl(permanentCouponImageUrl);
    }
    if (request.getNumberOfUses() != null) coupon.setNumberOfUses(request.getNumberOfUses());
    if (request.getIsPermanent() != null) coupon.setIsPermanent(request.getIsPermanent());

    couponRepository.save(coupon);
  }

  private void createNewCoupon(Membership membership, MembershipCouponUpdateRequest request) {
    MembershipCouponCreateRequest createRequest = convertToCouponCreateRequest(request);

    ValidationUtil.validateImage(this.cloudStorage, createRequest.getImageUrl());
    final long numberOfUses = request.getIsPermanent() ? 0 : request.getNumberOfUses();

    Coupon newCoupon =
        Coupon.builder()
            .name(createRequest.getName())
            .couponType(createRequest.getCouponType())
            .discountPercent(createRequest.getDiscountPercent())
            .periodInDays(createRequest.getPeriodInDays())
            .imageUrl(createRequest.getImageUrl())
            .membership(membership)
            .numberOfUses(numberOfUses)
            .isPermanent(createRequest.getIsPermanent())
            .fromSource(CouponSource.MEMBERSHIP_PACKAGE)
            .build();

    Coupon savedCoupon = couponRepository.save(newCoupon);

    if (StringUtils.isNotEmpty(createRequest.getImageUrl())) {
      String newCouponPath = FilePathType.COUPON.getPath() + "/" + savedCoupon.getId();
      String permanentCouponImageUrl =
          Constants.moveImageToNewPlace(
              this.cloudStorage, createRequest.getImageUrl(), newCouponPath);
      savedCoupon.setImageUrl(permanentCouponImageUrl);
      couponRepository.save(savedCoupon);
    }
  }
}
