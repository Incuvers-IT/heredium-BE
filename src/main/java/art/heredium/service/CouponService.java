package art.heredium.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import org.apache.commons.lang3.StringUtils;

import art.heredium.core.util.Constants;
import art.heredium.core.util.ValidationUtil;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.model.dto.request.CouponCreateRequest;
import art.heredium.domain.coupon.repository.CouponRepository;
import art.heredium.ncloud.bean.CloudStorage;

@Service
@RequiredArgsConstructor
public class CouponService {
  private final CouponRepository couponRepository;
  private final CloudStorage cloudStorage;

  public Long createNonMembershipCoupon(@NonNull final CouponCreateRequest request) {
    ValidationUtil.validateCouponRequest(request);
    ValidationUtil.validateImage(cloudStorage, request.getImageUrl());
    Coupon coupon =
        Coupon.builder()
            .name(request.getName())
            .couponType(request.getCouponType())
            .discountPercent(request.getDiscountPercent())
            .periodInDays(request.getPeriodInDays())
            .imageUrl(request.getImageUrl())
            .numberOfUses(request.getNumberOfUses())
            .isPermanent(request.getIsPermanent())
            .isNonMembershipCoupon(true)
            .build();

    Coupon savedCoupon = couponRepository.save(coupon);

    if (StringUtils.isNotEmpty(request.getImageUrl())) {
      // Move coupon image to permanent storage and update the imageUrl
      String newCouponPath = FilePathType.COUPON.getPath() + "/" + savedCoupon.getId();
      String permanentCouponImageUrl = moveImageToNewPlace(request.getImageUrl(), newCouponPath);
      savedCoupon.updateImageUrl(permanentCouponImageUrl);
      couponRepository.save(savedCoupon);
    }
    return savedCoupon.getId();
  }

  private String moveImageToNewPlace(String tempOriginalUrl, String newPath) {
    Storage storage = new Storage();
    storage.setSavedFileName(tempOriginalUrl);
    Constants.moveFileFromTemp(cloudStorage, storage, newPath);
    return storage.getSavedFileName();
  }
}
