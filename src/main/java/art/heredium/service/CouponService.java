package art.heredium.service;

import java.time.LocalDateTime;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.commons.lang3.StringUtils;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.Constants;
import art.heredium.core.util.ValidationUtil;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.model.dto.request.CouponCreateRequest;
import art.heredium.domain.coupon.model.dto.request.MembershipCouponCreateRequest;
import art.heredium.domain.coupon.model.dto.request.NonMembershipCouponCreateRequest;
import art.heredium.domain.coupon.repository.CouponRepository;
import art.heredium.domain.coupon.validation.CouponValidationUtil;
import art.heredium.domain.membership.entity.Membership;
import art.heredium.ncloud.bean.CloudStorage;

@Service
@RequiredArgsConstructor
public class CouponService {
  private final CouponRepository couponRepository;
  private final CloudStorage cloudStorage;

  public Long createNonMembershipCoupon(@NonNull final NonMembershipCouponCreateRequest request) {
    return createCoupon(request, null);
  }

  public Long createMembershipCoupon(
      @NonNull final MembershipCouponCreateRequest request, @NonNull final Membership membership) {
    return createCoupon(request, membership);
  }

  private Long createCoupon(
      @NonNull final CouponCreateRequest request, @Nullable final Membership membership) {
    final boolean isNonMembershipCoupon = request instanceof NonMembershipCouponCreateRequest;
    if ((!isNonMembershipCoupon && membership == null)
        || (isNonMembershipCoupon && membership != null)) {
      // This case will never happen
      throw new ApiException(
          ErrorCode.BAD_VALID,
          String.format(
              "Invalid coupon request for '%s': If 'isNonMembershipCoupon' is false, 'membership' must be provided. If 'isNonMembershipCoupon' is true, 'membership' must not be provided.",
              request.getName()));
    }
    Integer periodInDays = null;
    LocalDateTime startedDate = null;
    LocalDateTime endedDate = null;
    if (isNonMembershipCoupon) {
      startedDate = ((NonMembershipCouponCreateRequest) request).getStartDate();
      endedDate = ((NonMembershipCouponCreateRequest) request).getEndDate();
    } else {
      periodInDays = ((MembershipCouponCreateRequest) request).getPeriodInDays();
    }
    CouponValidationUtil.validateCouponRequest(request);

    ValidationUtil.validateImage(this.cloudStorage, request.getImageUrl());

    Coupon coupon =
        Coupon.builder()
            .name(request.getName())
            .couponType(request.getCouponType())
            .discountPercent(request.getDiscountPercent())
            .startedDate(startedDate)
            .endedDate(endedDate)
            .periodInDays(periodInDays)
            .imageUrl(request.getImageUrl())
            .membership(membership)
            .numberOfUses(request.getNumberOfUses())
            .isPermanent(request.getIsPermanent())
            .isNonMembershipCoupon(isNonMembershipCoupon)
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

    return savedCoupon.getId();
  }
}
