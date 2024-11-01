package art.heredium.service;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.lang3.StringUtils;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.Constants;
import art.heredium.core.util.ValidationUtil;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.model.dto.request.GetAllActiveMembershipsRequest;
import art.heredium.domain.membership.model.dto.request.MembershipCreateRequest;
import art.heredium.domain.membership.model.dto.response.ActiveMembershipRegistrationsResponse;
import art.heredium.domain.membership.repository.MembershipRegistrationRepository;
import art.heredium.domain.membership.repository.MembershipRepository;
import art.heredium.domain.post.entity.Post;
import art.heredium.domain.post.repository.PostRepository;
import art.heredium.ncloud.bean.CloudStorage;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class MembershipService {
  private static final Long DEFAULT_MEMBERSHIP_PERIOD = 364L; // days

  private final MembershipRepository membershipRepository;
  private final MembershipRegistrationRepository membershipRegistrationRepository;
  private final PostRepository postRepository;
  private final CouponService couponService;
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
      ValidationUtil.validateImage(this.cloudStorage, request.getImageUrl());

      Membership membership =
          Membership.builder()
              .name(request.getName())
              .period(DEFAULT_MEMBERSHIP_PERIOD)
              .price(request.getPrice())
              .isEnabled(request.getIsEnabled() == null || request.getIsEnabled())
              .imageUrl(request.getImageUrl())
              .post(post)
              .build();

      Membership savedMembership = membershipRepository.save(membership);
      membershipIds.add(savedMembership.getId());

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
                  this.couponService.createMembershipCoupon(couponRequest, savedMembership));
    }

    return membershipIds;
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

  public Page<ActiveMembershipRegistrationsResponse> listActiveMembershipsWithFilter(
      GetAllActiveMembershipsRequest request, Pageable pageable) {
    return this.membershipRegistrationRepository.getAllActiveMembershipRegistrations(
        request, pageable);
  }
}
