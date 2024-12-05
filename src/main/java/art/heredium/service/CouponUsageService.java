package art.heredium.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import art.heredium.domain.coupon.model.dto.response.CouponUsageCheckResponse;
import art.heredium.domain.coupon.model.dto.response.CouponUsageResponse;
import art.heredium.domain.coupon.repository.CouponRepository;
import art.heredium.domain.coupon.repository.CouponUsageRepository;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.repository.MembershipRegistrationRepository;
import art.heredium.ncloud.bean.HerediumAlimTalk;
import art.heredium.ncloud.type.AlimTalkTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponUsageService {
  private static final DateTimeFormatter COUPON_DATETIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  private final CouponUsageRepository couponUsageRepository;
  private final CouponRepository couponRepository;
  private final AccountRepository accountRepository;
  private final MembershipRegistrationRepository membershipRegistrationRepository;
  private final HerediumProperties herediumProperties;
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
          couponUsageRepository.findUnusedOrPermanentCoupons(accountId, coupon.getId()).stream()
              .sorted(Comparator.comparing(CouponUsage::getExpirationDate))
              .collect(Collectors.toList());

      responseDtos.add(new CouponResponseDto(coupon, usedCoupons, unusedCoupons));
    }

    return responseDtos;
  }

  public List<CouponUsage> findByMembershipRegistrationIdAndIsUsedTrue(final long accountId) {
    return this.couponUsageRepository.findByMembershipRegistrationIdAndIsUsedTrue(accountId);
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
    this.assignCouponToAccounts(coupon, accountIds, CouponSource.ADMIN_SITE, true);
  }

  @Transactional(rollbackFor = Exception.class)
  public List<CouponUsage> distributeMembershipAndCompanyCoupons(
      @NonNull Account account, @NonNull List<Coupon> coupons, boolean sendAlimtalk) {
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
                  coupon.getFromSource(),
                  sendAlimtalk));
        });
    return this.couponUsageRepository.saveAll(couponUsages);
  }

  public CouponUsageResponse getCouponUsageResponseByUuid(@NonNull final String uuid) {
    return new CouponUsageResponse(this.getCouponUsageByUuid(uuid));
  }

  @Transactional(rollbackFor = Exception.class)
  public void checkoutCouponUsage(String uuid) {
    Long accountId =
        AuthUtil.getCurrentUserAccountId()
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "User not found"));
    LocalDateTime now = LocalDateTime.now();
    final CouponUsage couponUsage = this.getCouponUsageByUuid(uuid);

    couponUsage.setUsedCount(couponUsage.getUsedCount() + 1);
    couponUsage.setIsUsed(true);
    couponUsage.setUsedDate(LocalDateTime.now());
    couponUsageRepository.save(couponUsage);
    List<CouponUsage> remainedCouponUsages =
        this.couponUsageRepository.findByAccountIdAndIsUsedFalseAndNotExpiredAndSource(
            accountId, CouponSource.MEMBERSHIP_PACKAGE);
    if (couponUsage.getCoupon().getMembership() == null) {
      log.info(
          "Ignore sendWithMembershipCouponUsedMessageToAlimTalk due to membership is null {}",
          couponUsage);
      return;
    }
    this.sendWithMembershipCouponUsedMessageToAlimTalk(couponUsage, remainedCouponUsages);
  }

  private CouponUsage getCouponUsageByUuid(@NonNull final String uuid) {
    LocalDateTime now = LocalDateTime.now();
    final CouponUsage couponUsage =
        couponUsageRepository
            .findByUuid(uuid)
            .orElseThrow(
                () ->
                    new ApiException(
                        ErrorCode.COUPON_USAGE_NOT_FOUND,
                        "Coupon usage not found by uuid " + uuid));

    if (couponUsage.getExpirationDate().isBefore(now)) {
      throw new ApiException(ErrorCode.COUPON_EXPIRED, "Coupon is expired");
    }

    if (couponUsage.getIsUsed() && !couponUsage.isPermanent()) {
      throw new ApiException(ErrorCode.COUPON_ALREADY_USED, "Coupon is already used");
    }
    return couponUsage;
  }

  private List<CouponUsage> assignCouponToAccounts(
      final Coupon coupon,
      @NonNull final List<Long> accountIds,
      @NonNull final CouponSource source,
      final boolean sendAlimtalk) {
    long numberOfUses = Optional.ofNullable(coupon.getNumberOfUses()).orElse(1L);
    boolean isPermanentCoupon = Boolean.TRUE.equals(coupon.getIsPermanent());
    List<CouponUsage> couponUsages = new ArrayList<>();
    Set<Long> accountIdSet = new HashSet<>(accountIds);
    Map<Long, Account> accountMap =
        this.accountRepository.findByIdIn(accountIdSet).stream()
            .collect(Collectors.toMap(Account::getId, account -> account));
    if (accountMap.entrySet().size() != accountIdSet.size()) {
      throw new ApiException(ErrorCode.USER_NOT_FOUND);
    }

    LocalDateTime now = LocalDateTime.now();
    Map<Account, CouponUsage> accountsToSendAlimTalk = new HashMap<>();

    accountMap.forEach(
        (accountId, account) -> {
          LocalDateTime couponStartedDate;
          LocalDateTime couponEndedDate;
          MembershipRegistration membershipRegistration = null;

          if (source == CouponSource.MEMBERSHIP_PACKAGE) {
            couponStartedDate = now;
            couponEndedDate = couponStartedDate.plusDays(coupon.getPeriodInDays());
          } else if (source == CouponSource.ADMIN_SITE) {
            couponStartedDate = coupon.getStartedDate();
            couponEndedDate = coupon.getEndedDate();
          } else if (source == CouponSource.COMPANY) {
            membershipRegistration =
                membershipRegistrationRepository
                    .findTopByAccountOrderByRegistrationDateDesc(account)
                    .orElseThrow(
                        () ->
                            new ApiException(
                                ErrorCode.MEMBERSHIP_REGISTRATION_NOT_FOUND,
                                "Membership registration not found with accountId: "
                                    + account.getId()));
            couponStartedDate = membershipRegistration.getRegistrationDate();
            couponEndedDate = couponStartedDate.plusDays(coupon.getPeriodInDays());
          } else {
            throw new ApiException(ErrorCode.INVALID_COUPON_SOURCE);
          }

          if (isPermanentCoupon) {
            CouponUsage couponUsage =
                new CouponUsage(
                    coupon,
                    account,
                    membershipRegistration,
                    couponStartedDate,
                    couponEndedDate,
                    true,
                    0L);
            couponUsages.add(couponUsage);
          } else {
            for (int i = 0; i < numberOfUses; i++) {
              CouponUsage couponUsage =
                  new CouponUsage(
                      coupon,
                      account,
                      membershipRegistration,
                      couponStartedDate,
                      couponEndedDate,
                      false,
                      0L);
              couponUsages.add(couponUsage);
            }
          }
          if (!couponUsages.isEmpty()) {
            // Each account can only be assigned one coupon type
            accountsToSendAlimTalk.put(account, couponUsages.get(0));
          }
        });
    if (sendAlimtalk) {
      this.sendCouponDeliveredMessageToAlimTalk(accountsToSendAlimTalk);
    }

    return this.couponUsageRepository.saveAll(couponUsages);
  }

  private void sendCouponDeliveredMessageToAlimTalk(
      final Map<Account, CouponUsage> accountsToSendAlimTalk) {
    log.info("Start sendCouponDeliveredMessageToAlimTalk {}", accountsToSendAlimTalk);
    Map<String, Map<String, String>> phonesAndMessagesToSendAlimTalk = new HashMap<>();
    try {
      accountsToSendAlimTalk.forEach(
          (account, coupon) -> {
            final Map<String, String> variables = new HashMap<>();
            variables.put("accountName", account.getAccountInfo().getName());
            variables.put("couponType", coupon.getCoupon().getCouponType().getDesc());
            variables.put("couponName", coupon.getCoupon().getName());
            variables.put(
                "discountPercent",
                coupon.getCoupon().getDiscountPercent() != 100
                    ? coupon.getCoupon().getDiscountPercent() + "%"
                    : "무료");
            variables.put(
                "couponStartDate", coupon.getDeliveredDate().format(COUPON_DATETIME_FORMAT));
            variables.put(
                "couponEndDate", coupon.getExpirationDate().format(COUPON_DATETIME_FORMAT));
            variables.put(
                "numberOfUses",
                coupon.isPermanent() ? "상시할인" : coupon.getCoupon().getNumberOfUses() + "회");
            variables.put("CSTel", herediumProperties.getTel());
            variables.put("CSEmail", herediumProperties.getEmail());
            phonesAndMessagesToSendAlimTalk.put(account.getAccountInfo().getPhone(), variables);
          });
      this.alimTalk.sendAlimTalkWithoutTitle(
          phonesAndMessagesToSendAlimTalk, AlimTalkTemplate.COUPON_HAS_BEEN_DELIVERED);
    } catch (Exception e) {
      log.warn(
          "Sending message to AlimTalk failed: {}, message params: {}",
          e.getMessage(),
          phonesAndMessagesToSendAlimTalk);
    } finally {
      log.info("End sendCouponDeliveredMessageToAlimTalk");
    }
  }

  public void rollbackCouponUsage(String couponUuid) {
    Optional<CouponUsage> couponUsageOptional = couponUsageRepository.findByUuid(couponUuid);
    if (!couponUsageOptional.isPresent())
      throw new ApiException(
          ErrorCode.COUPON_USAGE_NOT_FOUND, "Coupon usage not found by uuid " + couponUuid);
    CouponUsage couponUsage = couponUsageOptional.get();
    long usedCount = couponUsage.getUsedCount();
    couponUsage.setUsedCount(--usedCount);
    if (usedCount == 0) {
      couponUsage.setIsUsed(false);
      couponUsage.setUsedDate(null);
    }
    couponUsageRepository.save(couponUsage);
  }

  @Transactional(rollbackFor = Exception.class)
  public List<CouponUsage> rollbackCouponDistribution(Long membershipRegistrationId) {
    return this.couponUsageRepository.deleteByMembershipRegistrationId(membershipRegistrationId);
  }

  private void sendWithMembershipCouponUsedMessageToAlimTalk(
      final CouponUsage couponUsage, final List<CouponUsage> remainedCouponUsages) {
    log.info(
        "Start sendWithMembershipCouponUsedMessageToAlimTalk {}, {}",
        couponUsage,
        remainedCouponUsages);
    try {
      Map<String, String> params = new HashMap<>();
      params.put("accountName", couponUsage.getAccount().getAccountInfo().getName());
      params.put("membershipName", couponUsage.getCoupon().getMembership().getName());
      params.put("issuedDate", couponUsage.getDeliveredDate().format(COUPON_DATETIME_FORMAT));
      params.put("issuedCouponName", couponUsage.getCoupon().getName());
      params.put("remainedDetailCoupons", this.buildCouponDetails(remainedCouponUsages));
      params.put("CSTel", herediumProperties.getTel());
      params.put("CSEmail", herediumProperties.getEmail());

      this.alimTalk.sendAlimTalkWithoutTitle(
          couponUsage.getAccount().getAccountInfo().getPhone(),
          params,
          AlimTalkTemplate.COUPON_HAS_BEEN_USED);
    } catch (Exception e) {
      log.warn("Sending message to AlimTalk failed: {}", e.getMessage());
    } finally {
      log.info("End sendWithMembershipCouponUsedMessageToAlimTalk");
    }
  }

  private String buildCouponDetails(List<CouponUsage> coupons) {
    return coupons.stream()
        .map(
            coupon ->
                String.format(
                    " - %s, %s%% : %s",
                    coupon.getCoupon().getName(),
                    coupon.getCoupon().getDiscountPercent(),
                    Boolean.TRUE.equals(coupon.getCoupon().getIsPermanent())
                        ? "상시할인"
                        : coupon.getCoupon().getNumberOfUses()))
        .collect(Collectors.joining("\n"));
  }

  public CouponUsageCheckResponse checkActiveMembershipCouponUsage(final long accountId) {
    // Find active membership registration for the account
    MembershipRegistration activeMembership =
        membershipRegistrationRepository
            .findCompletedOneByAccountIdAndNotExpired(accountId)
            .orElse(null);

    if (activeMembership == null) {
      return new CouponUsageCheckResponse(false);
    }

    // Use existing method to check for used coupons
    List<CouponUsage> usedCoupons =
        findByMembershipRegistrationIdAndIsUsedTrue(activeMembership.getId());

    return new CouponUsageCheckResponse(!usedCoupons.isEmpty());
  }
}
