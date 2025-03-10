package art.heredium.service;

import java.time.LocalDateTime;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.lang3.StringUtils;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.Constants;
import art.heredium.core.util.ValidationUtil;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.domain.company.entity.Company;
import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.entity.CouponSource;
import art.heredium.domain.coupon.model.dto.request.CompanyCouponCreateRequest;
import art.heredium.domain.coupon.model.dto.request.CouponCreateRequest;
import art.heredium.domain.coupon.model.dto.request.MembershipCouponCreateRequest;
import art.heredium.domain.coupon.model.dto.request.NonMembershipCouponCreateRequest;
import art.heredium.domain.coupon.model.dto.response.CouponResponse;
import art.heredium.domain.coupon.repository.CouponRepository;
import art.heredium.domain.membership.entity.Membership;
import art.heredium.ncloud.bean.CloudStorage;

@Service
@RequiredArgsConstructor
public class CouponService {
  private final CouponRepository couponRepository;
  private final CloudStorage cloudStorage;

  @Transactional(rollbackFor = Exception.class)
  public CouponResponse createNonMembershipCoupon(
      @NonNull final NonMembershipCouponCreateRequest request) {
    return new CouponResponse(createCoupon(request, null, null));
  }

  @Transactional(rollbackFor = Exception.class)
  public Long createMembershipCoupon(
      @NonNull final MembershipCouponCreateRequest request, @NonNull final Membership membership) {
    return createCoupon(request, membership, null).getId();
  }

  @Transactional(rollbackFor = Exception.class)
  public Long createCompanyCoupon(
      @NonNull final CompanyCouponCreateRequest request, @NonNull final Company company) {
    return createCoupon(request, null, company).getId();
  }

  public Coupon createCoupon(
      @NonNull final CouponCreateRequest request,
      @Nullable final Membership membership,
      @Nullable final Company company) {
    final boolean isNonMembershipCoupon = request instanceof NonMembershipCouponCreateRequest;
    final boolean isMembershipCoupon = request instanceof MembershipCouponCreateRequest;
    final boolean isCompanyCoupon = request instanceof CompanyCouponCreateRequest;
    if ((isMembershipCoupon && membership == null) || (!isMembershipCoupon && membership != null)) {
      // This case will never happen
      throw new ApiException(
          ErrorCode.BAD_VALID,
          String.format(
              "Invalid coupon request for '%s': If 'isMembershipCoupon' is true, 'membership' must be provided. If 'isMembershipCoupon' is false, 'membership' must not be provided.",
              request.getName()));
    }
    if ((isCompanyCoupon && company == null) || (!isCompanyCoupon && company != null)) {
      // This case will never happen
      throw new ApiException(
          ErrorCode.BAD_VALID,
          String.format(
              "Invalid coupon request for '%s': If 'isCompanyCoupon' is true, 'company' must be provided. If 'isCompanyCoupon' is false, 'company' must not be provided.",
              request.getName()));
    }
    Integer periodInDays = null;
    LocalDateTime startedDate = null;
    LocalDateTime endedDate = null;
    CouponSource fromSource = CouponSource.fromCouponCreateRequest(request);
    if (isNonMembershipCoupon) {
      startedDate = ((NonMembershipCouponCreateRequest) request).getStartDate();
      endedDate = ((NonMembershipCouponCreateRequest) request).getEndDate();
    } else if (isMembershipCoupon) {
      periodInDays = ((MembershipCouponCreateRequest) request).getPeriodInDays();
    } else if (isCompanyCoupon) {
      periodInDays = ((CompanyCouponCreateRequest) request).getPeriodInDays();
    }

    ValidationUtil.validateImage(this.cloudStorage, request.getImageUrl());
    final long numberOfUses = request.getIsPermanent() ? 0 : request.getNumberOfUses();

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
            .company(company)
            .numberOfUses(numberOfUses)
            .isPermanent(request.getIsPermanent())
            .fromSource(fromSource)
            .build();

    Coupon savedCoupon = couponRepository.saveAndFlush(coupon);

    if (StringUtils.isNotEmpty(request.getImageUrl())) {
      // Move coupon image to permanent storage and update the imageUrl
      String newCouponPath = FilePathType.COUPON.getPath() + "/" + savedCoupon.getId();
      String permanentCouponImageUrl =
          Constants.moveImageToNewPlace(this.cloudStorage, request.getImageUrl(), newCouponPath);
      savedCoupon.updateImageUrl(permanentCouponImageUrl);
      couponRepository.saveAndFlush(savedCoupon);
    }

    return savedCoupon;
  }

  @NonNull
  public CouponResponse getCouponDetail(final long couponId) {
    final Coupon coupon =
        this.couponRepository
            .findById(couponId)
            .orElseThrow(
                () ->
                    new ApiException(ErrorCode.COUPON_NOT_FOUND, "CouponId not found " + couponId));
    return new CouponResponse(coupon);
  }
}
