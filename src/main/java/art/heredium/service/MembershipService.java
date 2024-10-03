package art.heredium.service;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipService {

  private static final Boolean DEFAULT_ENABLED_STATUS = true;

  private final MembershipRepository membershipRepository;
  private final PostRepository postRepository;
  private final CouponRepository couponRepository;

    public List<Membership> findByPostIdAndIsEnabledTrue(long postId) {
        return this.membershipRepository.findByPostIdAndIsEnabledTrue(postId);
    }

  @Transactional
  public List<Long> createMemberships(List<MembershipCreateRequest> membershipRequests) {
    List<Long> membershipIds = new ArrayList<>();

    for (MembershipCreateRequest request : membershipRequests) {
      Membership membership =
          Membership.builder()
              .name(request.getName())
              .period(request.getPeriod())
              .price(request.getPrice())
              .imageUrl(request.getImageUrl())
              .enabled(DEFAULT_ENABLED_STATUS)
              .build();

      Membership savedMembership = membershipRepository.save(membership);
      membershipIds.add(savedMembership.getId());

      for (MembershipCouponCreateRequest couponRequest : request.getCoupons()) {
        Coupon coupon =
            Coupon.builder()
                .name(couponRequest.getName())
                .couponType(couponRequest.getCouponType())
                .discountPercent(couponRequest.getDiscountPercent())
                .periodInDays(couponRequest.getPeriodInDays())
                .imageUrl(couponRequest.getImageUrl())
                .membership(savedMembership)
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
