package art.heredium.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.model.dto.request.MembershipCouponCreateRequest;
import art.heredium.domain.coupon.repository.CouponRepository;
import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.model.dto.request.MembershipCreateRequest;
import art.heredium.domain.membership.repository.MembershipRepository;
import art.heredium.domain.post.entity.Post;
import art.heredium.domain.post.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class MembershipService {

  private static final boolean DEFAULT_ENABLED_STATUS = true;
  private static final Long DEFAULT_MEMBERSHIP_PERIOD = 12L; // months

  private final MembershipRepository membershipRepository;
  private final PostRepository postRepository;
  private final CouponRepository couponRepository;

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
      Membership membership =
          Membership.builder()
              .name(request.getName())
              .period(DEFAULT_MEMBERSHIP_PERIOD)
              .price(request.getPrice())
              .isEnabled(DEFAULT_ENABLED_STATUS)
              .post(post)
              .build();

      Membership savedMembership = membershipRepository.save(membership);
      membershipIds.add(savedMembership.getId());

      for (MembershipCouponCreateRequest couponRequest : request.getCoupons()) {
        if ((!couponRequest.getIsPermanent() && couponRequest.getNumberOfUses() == null)
            || (couponRequest.getIsPermanent() && couponRequest.getNumberOfUses() != null)) {
          throw new ApiException(
              ErrorCode.BAD_VALID,
              String.format(
                  "Invalid coupon request for '%s': If 'isPermanent' is false, 'numberOfUses' must be provided. If 'isPermanent' is true, 'numberOfUses' must not be provided.",
                  couponRequest.getName()));
        }
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

        couponRepository.save(coupon);
      }
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
}
