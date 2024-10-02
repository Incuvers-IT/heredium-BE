package art.heredium.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.entity.CouponUsage;
import art.heredium.domain.coupon.model.dto.response.CouponResponseDto;
import art.heredium.domain.coupon.repository.CouponUsageRepository;

@Service
@RequiredArgsConstructor
public class CouponUsageService {
  private final CouponUsageRepository couponUsageRepository;

  public List<CouponResponseDto> getCouponsWithUsageByAccountId(Long accountId) {
    List<Coupon> coupons = couponUsageRepository.findDistinctCouponsByAccountId(accountId);
    List<CouponResponseDto> responseDtos = new ArrayList<>();

    for (Coupon coupon : coupons) {
      List<CouponUsage> usedCoupons =
          couponUsageRepository
              .findByAccountIdAndCouponIdAndIsUsed(accountId, coupon.getId(), true)
              .stream()
              .toList();

      List<CouponUsage> unusedCoupons =
          couponUsageRepository
              .findByAccountIdAndCouponIdAndIsUsed(accountId, coupon.getId(), false)
              .stream()
              .sorted(Comparator.comparing(CouponUsage::getExpirationDate))
              .toList();

      responseDtos.add(new CouponResponseDto(coupon, usedCoupons, unusedCoupons));
    }

    return responseDtos;
  }
}
