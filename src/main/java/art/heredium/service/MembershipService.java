package art.heredium.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.entity.CouponSource;
import art.heredium.domain.coupon.model.dto.request.MembershipCouponCreateRequest;
import art.heredium.domain.coupon.model.dto.response.CouponResponse;
import art.heredium.domain.coupon.repository.CouponRepository;
import art.heredium.domain.membership.model.dto.request.MembershipUpdateCouponRequest;
import art.heredium.domain.membership.model.dto.request.MembershipUpdateRequest;
import art.heredium.domain.membership.model.dto.response.ActiveMembershipDetailResponse;
import art.heredium.domain.membership.model.dto.response.MembershipOptionResponse;
import art.heredium.domain.membership.model.dto.response.MembershipResponse;
import art.heredium.domain.post.model.dto.request.MembershipCouponUpdateRequest;
import art.heredium.domain.post.model.dto.request.PostMembershipUpdateRequest;
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
      // Validate membership image
      ValidationUtil.validateImage(this.cloudStorage, request.getImageUrl());

      Membership membership =
          Membership.builder()
              .name(request.getName())
              .isEnabled(request.getIsEnabled() == null || request.getIsEnabled())
              .imageUrl(request.getImageUrl())
              .post(post)
              .usageThreshold(0)
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

  public Page<ActiveMembershipDetailResponse> listActiveMembershipsWithFilterDetail(
          GetAllActiveMembershipsRequest request, Pageable pageable) {
    return this.membershipRegistrationRepository.getActiveMembershipRegistrations(
            request, pageable);
  }

  @Transactional
  public boolean createMembership(MembershipCreateRequest request) {

    Post post = postRepository.findById(1L)
            .orElseThrow(() -> new ApiException(ErrorCode.POST_NOT_FOUND));

    Membership membership =
            Membership.builder()
                    .name(request.getName())
                    .shortName(request.getShortName())
                    .isEnabled(request.getIsEnabled() == null || request.getIsEnabled())
                    .imageUrl(request.getImageUrl())
                    .post(post)
                    .usageThreshold(request.getUsageThreshold())
                    .build();

    Membership savedMembership = membershipRepository.save(membership);

    if (StringUtils.isNotEmpty(request.getImageUrl())) {
      ValidationUtil.validateImage(this.cloudStorage, request.getImageUrl());
      // Move membership image to permanent storage and update the imageUrl
      String newMembershipPath =
              FilePathType.MEMBERSHIP.getPath() + "/" + savedMembership.getId();
      String permanentImageUrl =
              Constants.moveImageToNewPlace(
                      this.cloudStorage, request.getImageUrl(), newMembershipPath);
      savedMembership.updateImageUrl(permanentImageUrl);
      membershipRepository.saveAndFlush(savedMembership);
    }

    request
        .getCoupons()
        .forEach(
                couponRequest ->
                        this.couponService.createMembershipCoupon(couponRequest, savedMembership));

    return true;
  }

  public List<MembershipOptionResponse> listMembershipOptions() {
    List<Membership> all = membershipRepository.findAll();
    return all.stream()
            .map(m -> new MembershipOptionResponse(m.getId(), m.getName()))
            .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public MembershipResponse getMembershipDetail(long membershipId) {
    Membership m = membershipRepository.findById(membershipId)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMBERSHIP_NOT_FOUND));

    // MembershipResponse 생성자 내부에서 CouponResponse 로 매핑해 줍니다
    return new MembershipResponse(m);
  }

  @Transactional
  public void updateMembership(long membershipId, MembershipUpdateCouponRequest request) {
    // 1) 기존 멤버십 조회
    Membership m = membershipRepository.findById(membershipId)
            .orElseThrow(() -> new ApiException(
                    ErrorCode.MEMBERSHIP_NOT_FOUND, "id=" + membershipId));

    // 2) 필드 업데이트
    if (request.getName() != null) {
      m.setName(request.getName());
    }

    if (request.getImageUrl() != null) {
      ValidationUtil.validateImage(cloudStorage, request.getImageUrl());
      String newPath = FilePathType.MEMBERSHIP.getPath() + "/" + m.getId();
      String url = Constants.moveImageToNewPlace(
              cloudStorage, request.getImageUrl(), newPath);
      m.setImageUrl(url);
    }

    if (request.getIsEnabled() != null) {
      m.setIsEnabled(request.getIsEnabled());
    }

    if (request.getUsageThreshold() != null) {
      m.setUsageThreshold(request.getUsageThreshold());
    }

    m.setShortName(request.getShortName());

    membershipRepository.save(m);

    // 3) 쿠폰들 업데이트
    updateCoupons(m, request.getCoupons());
  }

  private void updateCoupons(
          Membership membership, List<MembershipCouponUpdateRequest> couponRequests) {

    // 요청으로 들어온(남아 있어야 할) 쿠폰 ID 목록
    List<Long> incomingIds = couponRequests.stream()
            .map(MembershipCouponUpdateRequest::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    // 1) 소프트 삭제: 기존 쿠폰 중, 요청에 없는 것들
    membership.getCoupons().stream()
            .filter(c -> !incomingIds.contains(c.getId()))
            .forEach(c -> {
              c.setDeleted(true);              // <-- soft‐delete 플래그 세팅
              couponRepository.save(c);
            });

    // 2) 나머지 (업데이트 / 생성)
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
}
