package art.heredium.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.config.properties.HerediumProperties;
import art.heredium.core.util.AuthUtil;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.repository.AccountRepository;
import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.entity.CouponUsage;
import art.heredium.domain.coupon.model.dto.response.CouponResponseDto;
import art.heredium.domain.coupon.repository.CouponUsageRepository;
import art.heredium.ncloud.bean.HerediumAlimTalk;
import art.heredium.ncloud.type.AlimTalkTemplate;

@Service
@RequiredArgsConstructor
public class CouponUsageService {
  private final HerediumProperties herediumProperties;
  private final CouponUsageRepository couponUsageRepository;
  private final AccountRepository accountRepository;
  private final HerediumAlimTalk alimTalk;

  public List<CouponResponseDto> getCouponsWithUsageByAccountId(Long accountId) {
    List<Coupon> coupons = couponUsageRepository.findDistinctCouponsByAccountId(accountId);
    List<CouponResponseDto> responseDtos = new ArrayList<>();

    for (Coupon coupon : coupons) {
      List<CouponUsage> usedCoupons =
          couponUsageRepository
              .findByAccountIdAndCouponIdAndIsUsedTrue(accountId, coupon.getId())
              .stream()
              .collect(Collectors.toList());

      List<CouponUsage> unusedCoupons =
          couponUsageRepository
              .findByAccountIdAndCouponIdAndIsUsedFalse(accountId, coupon.getId())
              .stream()
              .sorted(Comparator.comparing(CouponUsage::getExpirationDate))
              .collect(Collectors.toList());

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
                      0L);
              couponUsages.add(couponUsage);
            }
          }
        });
    this.alimTalk.sendAlimTalk(
        account.getAccountInfo().getPhone(),
        getCouponUsageListParams(couponUsages),
        AlimTalkTemplate.ALIMTALK_TEMPLATE_CODE);
    return this.couponUsageRepository.saveAll(couponUsages);
  }

  @Transactional(rollbackFor = Exception.class)
  public void checkoutCouponUsage(String uuid) {
    final long accountId =
        AuthUtil.getCurrentUserAccountId()
            .orElseThrow(() -> new ApiException(ErrorCode.ANONYMOUS_USER));
    final Account account =
        this.accountRepository
            .findById(accountId)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    CouponUsage couponUsage =
        couponUsageRepository
            .findByUuid(uuid)
            .orElseThrow(() -> new ApiException(ErrorCode.COUPON_NOT_FOUND, "Coupon not found"));

    LocalDateTime now = LocalDateTime.now();

    if (couponUsage.getExpirationDate().isBefore(now)) {
      throw new ApiException(ErrorCode.COUPON_EXPIRED, "Coupon is expired");
    }

    if (couponUsage.getIsUsed() && !couponUsage.isPermanent()) {
      throw new ApiException(ErrorCode.COUPON_ALREADY_USED, "Coupon is already used");
    }

    couponUsage.setUsedCount(couponUsage.getUsedCount() + 1);
    couponUsage.setIsUsed(true);

    couponUsage.setUsedDate(now);
    couponUsageRepository.save(couponUsage);
    this.alimTalk.sendAlimTalk(
        account.getAccountInfo().getPhone(),
        couponUsage.getCouponUsageParams(herediumProperties),
        AlimTalkTemplate.ALIMTALK_TEMPLATE_CODE);
  }

  private Map<String, String> getCouponUsageListParams(List<CouponUsage> couponUsages) {
    Map<String, String> params = new HashMap<>();
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("[");
    couponUsages.forEach(
        couponUsage -> {
          Map<String, String> couponUsageParams =
              couponUsage.getCouponUsageParams(herediumProperties);
          stringBuilder.append("{");
          couponUsageParams.forEach(
              (key, value) -> {
                stringBuilder.append(String.join("=", key, value));
                stringBuilder.append(", ");
              });
          stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
          stringBuilder.append("}");
          stringBuilder.append(", ");
        });
    stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
    stringBuilder.append("]");
    params.put("coupons", new String(stringBuilder));
    return params;
  }
}
