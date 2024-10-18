package art.heredium.service;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.lang3.StringUtils;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.Constants;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.model.dto.request.MembershipCouponCreateRequest;
import art.heredium.domain.coupon.repository.CouponRepository;
import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.model.dto.request.MembershipCreateRequest;
import art.heredium.domain.membership.repository.MembershipRepository;
import art.heredium.domain.post.entity.Post;
import art.heredium.domain.post.repository.PostRepository;
import art.heredium.ncloud.bean.CloudStorage;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class MembershipService {

  private static final boolean DEFAULT_ENABLED_STATUS = true;
  private static final Long DEFAULT_MEMBERSHIP_PERIOD = 12L; // months

  private final MembershipRepository membershipRepository;
  private final PostRepository postRepository;
  private final CouponRepository couponRepository;
  private final CloudStorage cloudStorage;

  public List<Membership> findByPostIdAndIsEnabledTrue(long postId) {
    return this.membershipRepository.findByPostIdAndIsEnabledTrue(postId);
  }

  @Transactional
  public List<Long> createMemberships(
      Long postId, List<MembershipCreateRequest> membershipRequests) {
    final List<Long> membershipIds = new ArrayList<>();
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
      validateImage(request.getImageUrl());

      Membership membership =
          Membership.builder()
              .name(request.getName())
              .period(DEFAULT_MEMBERSHIP_PERIOD)
              .price(request.getPrice())
              .isEnabled(DEFAULT_ENABLED_STATUS)
              .imageUrl(request.getImageUrl())
              .post(post)
              .build();

      Membership savedMembership = membershipRepository.save(membership);
      membershipIds.add(savedMembership.getId());

      if (StringUtils.isNotEmpty(request.getImageUrl())) {
        // Move membership image to permanent storage and update the imageUrl
        String newMembershipPath =
            FilePathType.MEMBERSHIP.getPath() + "/" + savedMembership.getId();
        String permanentImageUrl = moveImageToNewPlace(request.getImageUrl(), newMembershipPath);
        savedMembership.updateImageUrl(permanentImageUrl);
        membershipRepository.save(savedMembership);
      }

      for (MembershipCouponCreateRequest couponRequest : request.getCoupons()) {
        validateCouponRequest(couponRequest);

        validateImage(couponRequest.getImageUrl());

        Coupon coupon =
            Coupon.builder()
                .name(couponRequest.getName())
                .couponType(couponRequest.getCouponType())
                .discountPercent(couponRequest.getDiscountPercent())
                .periodInDays(couponRequest.getPeriodInDays())
                .imageUrl(couponRequest.getImageUrl())
                .membership(savedMembership)
                .numberOfUses(couponRequest.getNumberOfUses())
                .isPermanent(couponRequest.getIsPermanent())
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);

        if (StringUtils.isNotEmpty(couponRequest.getImageUrl())) {
          // Move coupon image to permanent storage and update the imageUrl
          String newCouponPath = FilePathType.COUPON.getPath() + "/" + savedCoupon.getId();
          String permanentCouponImageUrl =
              moveImageToNewPlace(couponRequest.getImageUrl(), newCouponPath);
          savedCoupon.updateImageUrl(permanentCouponImageUrl);
          couponRepository.save(savedCoupon);
        }
      }
    }

    return membershipIds;
  }

  private void validateImage(String imageUrl) {
    if (StringUtils.isNotEmpty(imageUrl) && !cloudStorage.isExistObject(imageUrl)) {
      throw new ApiException(ErrorCode.S3_NOT_FOUND, imageUrl);
    }
  }

  private String moveImageToNewPlace(String tempOriginalUrl, String newPath) {
    Storage storage = new Storage();
    storage.setSavedFileName(tempOriginalUrl);
    Constants.moveFileFromTemp(cloudStorage, storage, newPath);
    return storage.getSavedFileName();
  }

  private void validateCouponRequest(MembershipCouponCreateRequest couponRequest) {
    if ((!couponRequest.getIsPermanent() && couponRequest.getNumberOfUses() == null)
        || (couponRequest.getIsPermanent() && couponRequest.getNumberOfUses() != null)) {
      throw new ApiException(
          ErrorCode.BAD_VALID,
          String.format(
              "Invalid coupon request for '%s': If 'isPermanent' is false, 'numberOfUses' must be provided. If 'isPermanent' is true, 'numberOfUses' must not be provided.",
              couponRequest.getName()));
    }
  }

  @Transactional(rollbackFor = Exception.class)
  public void updateIsEnabled(long membershipId, boolean isEnabled) {
    Membership existingMembership =
        this.membershipRepository
            .findById(membershipId)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMBERSHIP_NOT_FOUND));
    Post existingPost =
        this.postRepository
            .findById(existingMembership.getPost().getId())
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));
    if (existingMembership.getIsEnabled() == isEnabled) {
      return;
    }
    if (isEnabled && !existingPost.getIsEnabled()) {
      throw new ApiException(ErrorCode.INVALID_POST_STATUS_TO_ENABLE_MEMBERSHIP);
    }
    existingMembership.updateIsEnabled(isEnabled);
  }
}
