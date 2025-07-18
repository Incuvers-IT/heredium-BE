package art.heredium.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import art.heredium.domain.coupon.model.dto.request.*;
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
      @NonNull final RecurringCouponCreateRequest request) {
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

  private Coupon createCoupon(
          @NonNull final CouponCreateRequest request,
          @Nullable final Membership membership,
          @Nullable final Company company) {

    final boolean isRecurringCoupon = request instanceof RecurringCouponCreateRequest;
    final boolean isNonMembershipCoupon = request instanceof NonMembershipCouponCreateRequest;
    final boolean isMembershipCoupon = request instanceof MembershipCouponCreateRequest;
    final boolean isCompanyCoupon = request instanceof CompanyCouponCreateRequest;

    if ((isMembershipCoupon && membership == null) || (!isMembershipCoupon && membership != null)) {
      throw new ApiException(
              ErrorCode.BAD_VALID,
              String.format(
                      "Invalid coupon request for '%s': If 'isMembershipCoupon' is true, 'membership' must be provided. If 'isMembershipCoupon' is false, 'membership' must not be provided.",
                      request.getName()));
    }

    if ((isCompanyCoupon && company == null) || (!isCompanyCoupon && company != null)) {
      throw new ApiException(
              ErrorCode.BAD_VALID,
              String.format(
                      "Invalid coupon request for '%s': If 'isCompanyCoupon' is true, 'company' must be provided. If 'isCompanyCoupon' is false, 'company' must not be provided.",
                      request.getName()));
    }

    Integer periodInDays = null;
    LocalDateTime startedDate = null;
    LocalDateTime endedDate = null;

    Boolean isRecurring = null;
    List<Short> recipientType = null;
    Integer sendDayOfMonth = null;
    Boolean marketingConsentBenefit = null;

    CouponSource fromSource = CouponSource.fromCouponCreateRequest(request);

    if (isRecurringCoupon) {
      RecurringCouponCreateRequest recurringReq = (RecurringCouponCreateRequest) request;
      isRecurring = recurringReq.getIsRecurring();
      recipientType = recurringReq.getRecipientType();
      sendDayOfMonth = recurringReq.getSendDayOfMonth();
      marketingConsentBenefit = recurringReq.getMarketingConsentBenefit();

      if (Boolean.TRUE.equals(isRecurring) || Boolean.TRUE.equals(marketingConsentBenefit)) {
        periodInDays = recurringReq.getPeriodInDays();
      } else {
        startedDate = recurringReq.getStartDate();
        endedDate = recurringReq.getEndDate();
      }

    } else if (isNonMembershipCoupon) {
      startedDate = ((NonMembershipCouponCreateRequest) request).getStartDate();
      endedDate = ((NonMembershipCouponCreateRequest) request).getEndDate();
    } else if (isMembershipCoupon) {
      periodInDays = ((MembershipCouponCreateRequest) request).getPeriodInDays();
    } else if (isCompanyCoupon) {
      periodInDays = ((CompanyCouponCreateRequest) request).getPeriodInDays();
    }

    ValidationUtil.validateImage(this.cloudStorage, request.getImageUrl());

    final long numberOfUses = request.getIsPermanent() ? 0 : request.getNumberOfUses();

    Coupon coupon = Coupon.builder()
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
            .isRecurring(isRecurring)
            .recipientType(recipientType)
            .sendDayOfMonth(sendDayOfMonth)
            .marketingConsentBenefit(marketingConsentBenefit)
            .build();

    Coupon savedCoupon = couponRepository.save(coupon);

    if (StringUtils.isNotEmpty(request.getImageUrl())) {
      String newCouponPath = FilePathType.COUPON.getPath() + "/" + savedCoupon.getId();
      String permanentCouponImageUrl =
              Constants.moveImageToNewPlace(this.cloudStorage, request.getImageUrl(), newCouponPath);
      savedCoupon.updateImageUrl(permanentCouponImageUrl);
      couponRepository.save(savedCoupon);
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

  public List<CouponResponse> getAllCoupons() {
    return couponRepository.findAll().stream()
            .map(CouponResponse::new)
            .collect(Collectors.toList());
  }

  /**
   * 논리 삭제: isDeleted=true 로 플래그 변경
   */
  @Transactional
  public void deleteCoupon(Long couponId) {
    Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new ApiException(ErrorCode.COUPON_NOT_FOUND));
    coupon.markDeleted();
    couponRepository.save(coupon);
  }

  @Transactional(rollbackFor = Exception.class)
  public CouponResponse updateCoupon(long couponId, RecurringCouponCreateRequest request) {
    // 1) 기존 쿠폰 조회
    Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new ApiException(ErrorCode.COUPON_NOT_FOUND, "CouponId not found " + couponId));

    // 2) Recurring 여부 판별 (createCoupon 과 동일하게!)
    boolean isRecurringCoupon = Boolean.TRUE.equals(request.getIsRecurring());
    boolean isMarketing = Boolean.TRUE.equals(request.getMarketingConsentBenefit());

    // 3) 발송 기간 정보 결정
    Integer periodInDays = null;
    LocalDateTime startDate   = null;
    LocalDateTime endDate     = null;
    if (isRecurringCoupon || isMarketing) {
      periodInDays = request.getPeriodInDays();
    } else {
      startDate = request.getStartDate();
      endDate   = request.getEndDate();
    }

    // 4) 이미지 URL 유효성 검사 (필요 시)
    ValidationUtil.validateImage(cloudStorage, request.getImageUrl());

    // 5) Coupon 엔티티 필드 덮어쓰기
    coupon.setName(request.getName());
    coupon.setCouponType(request.getCouponType());
    coupon.setDiscountPercent(request.getDiscountPercent());
    coupon.setImageUrl(request.getImageUrl());
    coupon.setIsRecurring(request.getIsRecurring());
    coupon.setMarketingConsentBenefit(request.getMarketingConsentBenefit());
    coupon.setRecipientType(request.getRecipientType());
    coupon.setSendDayOfMonth(request.getSendDayOfMonth());
    coupon.setIsPermanent(request.getIsPermanent());
    // numberOfUses: 상시 할인일 땐 0, 아니면 요청값
    long numUses = request.getIsPermanent() ? 0L : request.getNumberOfUses();
    coupon.setNumberOfUses(numUses);

    if (isRecurringCoupon || isMarketing) {
      coupon.setPeriodInDays(periodInDays);
      coupon.setStartedDate(null);
      coupon.setEndedDate(null);
    } else {
      coupon.setStartedDate(startDate);
      coupon.setEndedDate(endDate);
      coupon.setPeriodInDays(null);
    }

    // 6) DB 저장
    Coupon updated = couponRepository.save(coupon);

    // 7) 이미지가 새 경로로 이동되어야 한다면 createCoupon 과 동일하게 처리
    if (StringUtils.isNotBlank(request.getImageUrl())) {
      String newPath = FilePathType.COUPON.getPath() + "/" + updated.getId();
      String migrated = Constants.moveImageToNewPlace(cloudStorage, request.getImageUrl(), newPath);
      updated.updateImageUrl(migrated);
      couponRepository.save(updated);
    }

    // 8) 응답 생성
    return new CouponResponse(updated);
  }
}
