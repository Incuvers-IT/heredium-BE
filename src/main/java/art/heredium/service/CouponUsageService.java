package art.heredium.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
      List<CouponUsage> usedCouponUsages =
          couponUsageRepository.findByAccountIdAndCouponId(accountId, coupon.getId()).stream()
              .filter(CouponUsage::getIsUsed)
              .collect(Collectors.toList());

      List<CouponUsage> unusedCouponUsages =
          couponUsageRepository.findByAccountIdAndCouponId(accountId, coupon.getId()).stream()
              .filter(couponUsage -> !couponUsage.getIsUsed())
              .sorted(
                  Comparator.comparing(CouponUsage::getExpirationDate)) // Sort by expirationDate
              .collect(Collectors.toList());

      responseDtos.add(new CouponResponseDto(coupon, usedCouponUsages, unusedCouponUsages));
    }

    return responseDtos;
  }
}
