package art.heredium.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import art.heredium.domain.coupon.entity.CouponSource;
import art.heredium.domain.coupon.entity.CouponUsage;
import art.heredium.domain.coupon.model.dto.response.CouponResponseDto;
import art.heredium.domain.coupon.model.dto.response.CouponUsageResponse;
import art.heredium.domain.coupon.repository.CouponRepository;
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
  private final CouponRepository couponRepository;

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
  public void assignCoupons(final long couponId, @NonNull List<Long> accountIds) {
    final Coupon coupon =
        this.couponRepository
            .findById(couponId)
            .orElseThrow(() -> new ApiException(ErrorCode.COUPON_NOT_FOUND));
    if (coupon.getFromSource() == CouponSource.MEMBERSHIP_PACKAGE) {
      throw new ApiException(
          ErrorCode.INVALID_COUPON_TO_ASSIGN,
          String.format(
              "Error while assigning coupon: %s: This coupon is membership coupon",
              coupon.getName()));
    }
    this.assignCouponToAccounts(coupon, accountIds, CouponSource.ADMIN_SITE);
  }

  @Transactional(rollbackFor = Exception.class)
  public List<CouponUsage> distributeMembershipAndCompanyCoupons(
      @NonNull Account account, @NonNull List<Coupon> coupons) {
    List<CouponUsage> couponUsages = new ArrayList<>();
    coupons.forEach(
        coupon -> {
          if (coupon.getFromSource() == CouponSource.ADMIN_SITE) {
            throw new ApiException(
                ErrorCode.INVALID_COUPON_TO_ASSIGN,
                String.format(
                    "Error while assigning coupon: %s: This coupon is not membership coupon",
                    coupon.getName()));
          }
          couponUsages.addAll(
              this.assignCouponToAccounts(
                  coupon,
                  Stream.of(account.getId()).collect(Collectors.toList()),
                  coupon.getFromSource()));
        });
    this.alimTalk.sendAlimTalk(
        account.getAccountInfo().getPhone(),
        getCouponUsageListParams(couponUsages),
        AlimTalkTemplate.ALIMTALK_TEMPLATE_CODE);
    return this.couponUsageRepository.saveAll(couponUsages);
  }

  public CouponUsageResponse getCouponUsageByUuid(@NonNull final String uuid) {
    return new CouponUsageResponse(this.getCouponUsageByUuid(uuid, LocalDateTime.now()));
  }

  @Transactional(rollbackFor = Exception.class)
  public void checkoutCouponUsage(String uuid) {
    LocalDateTime now = LocalDateTime.now();
    final CouponUsage couponUsage = this.getCouponUsageByUuid(uuid, now);

    couponUsage.setUsedCount(couponUsage.getUsedCount() + 1);
    couponUsage.setIsUsed(true);

    couponUsage.setUsedDate(now);
    couponUsageRepository.save(couponUsage);
  }

  private CouponUsage getCouponUsageByUuid(
      @NonNull final String uuid, @NonNull final LocalDateTime now) {
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
      return couponUsage;
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
  private List<CouponUsage> assignCouponToAccounts(
      final Coupon coupon,
      @NonNull final List<Long> accountIds,
      @NonNull final CouponSource source) {
    long numberOfUses = Optional.ofNullable(coupon.getNumberOfUses()).orElse(1L);
    boolean isPermanentCoupon = Boolean.TRUE.equals(coupon.getIsPermanent());
    LocalDateTime startedDate = null;
    LocalDateTime endedDate = null;
    if (source == CouponSource.MEMBERSHIP_PACKAGE) {
      startedDate = LocalDateTime.now();
      endedDate = startedDate.plusDays(coupon.getPeriodInDays());
    } else if (source == CouponSource.ADMIN_SITE) {
      startedDate = coupon.getStartedDate();
      endedDate = coupon.getEndedDate();
    }
    List<CouponUsage> couponUsages = new ArrayList<>();
    LocalDateTime couponStartedDate = startedDate;
    LocalDateTime couponEndedDate = endedDate;
    Set<Long> accountIdSet = new HashSet<>(accountIds);
    Map<Long, Account> accountMap =
        this.accountRepository.findByIdIn(accountIdSet).stream()
            .collect(Collectors.toMap(Account::getId, account -> account));
    if (accountMap.entrySet().size() != accountIdSet.size()) {
      throw new ApiException(ErrorCode.USER_NOT_FOUND);
    }
    accountMap.forEach(
        (accountId, account) -> {
          if (isPermanentCoupon) {
            CouponUsage couponUsage =
                new CouponUsage(coupon, account, couponStartedDate, couponEndedDate, true, 0L);
            couponUsages.add(couponUsage);
          } else {
            for (int i = 0; i < numberOfUses; i++) {
              CouponUsage couponUsage =
                  new CouponUsage(coupon, account, couponStartedDate, couponEndedDate, false, 0L);
              couponUsages.add(couponUsage);
            }
          }
        });
    return this.couponUsageRepository.saveAll(couponUsages);
  }
}
