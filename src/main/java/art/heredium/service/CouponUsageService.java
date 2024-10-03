package art.heredium.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.domain.account.entity.Account;
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
              .findByAccountIdAndCouponIdAndIsUsedTrue(accountId, coupon.getId())
              .stream()
              .toList();

      List<CouponUsage> unusedCoupons =
          couponUsageRepository
              .findByAccountIdAndCouponIdAndIsUsedFalse(accountId, coupon.getId())
              .stream()
              .sorted(Comparator.comparing(CouponUsage::getExpirationDate))
              .toList();

      responseDtos.add(new CouponResponseDto(coupon, usedCoupons, unusedCoupons));
    }

    return responseDtos;
  }

  @Transactional(rollbackFor = Exception.class)
  public List<CouponUsage> distributeCoupons(
      @NonNull Account account, @NonNull List<Coupon> coupons) {
    LocalDateTime distributionDateTime = LocalDateTime.now();
    List<CouponUsage> couponUsages = new ArrayList<>();
    coupons.forEach(
        coupon -> {
          long numberOfUses = Optional.ofNullable(coupon.getNumberOfUses()).orElse(1L);
          boolean isPermanentCoupon = Boolean.TRUE.equals(coupon.getIsPermanent());
          if (isPermanentCoupon) {
            CouponUsage couponUsage =
                new CouponUsage(
                    coupon,
                    account,
                    distributionDateTime,
                    distributionDateTime.plusDays(coupon.getPeriodInDays()),
                    true,
                    0L);
            couponUsages.add(couponUsage);
          } else {
            for (int i = 0; i < numberOfUses; i++) {
              CouponUsage couponUsage =
                  new CouponUsage(
                      coupon,
                      account,
                      distributionDateTime,
                      distributionDateTime.plusDays(coupon.getPeriodInDays()),
                      false,
                      null);
              couponUsages.add(couponUsage);
            }
          }
        });
    return this.couponUsageRepository.saveAll(couponUsages);
  }
}
