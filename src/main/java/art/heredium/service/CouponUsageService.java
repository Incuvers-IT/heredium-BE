package art.heredium.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
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

  private static final DateTimeFormatter COUPON_DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private final CouponUsageRepository couponUsageRepository;
  private final CouponRepository couponRepository;
  private final AccountRepository accountRepository;
  private final MembershipRegistrationRepository membershipRegistrationRepository;
  private final HerediumProperties herediumProperties;
  private final HerediumAlimTalk alimTalk;

  public List<CouponResponseDto> getCouponsWithUsageByAccountId(Long accountId) {
    List<Coupon> coupons =
        couponUsageRepository.findDistinctCouponsByAccountIdAndIsNotDeleted(accountId);
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
//          if (coupon.getFromSource() == CouponSource.ADMIN_SITE) {
//            throw new ApiException(
//                ErrorCode.INVALID_COUPON_TO_ASSIGN,
//                String.format(
//                    "Error while assigning coupon: %s: This coupon is not membership coupon",
//                    coupon.getName()));
//          }
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
    final CouponUsage couponUsage = this.getCouponUsageByUuid(uuid);

    couponUsage.setUsedCount(couponUsage.getUsedCount() + 1);
    couponUsage.setIsUsed(true);
    couponUsage.setUsedDate(LocalDateTime.now());
    couponUsageRepository.save(couponUsage);
    if (couponUsage.getCoupon().getFromSource() == CouponSource.MEMBERSHIP_PACKAGE) {
      if (couponUsage.getCoupon().getMembership() == null) {
        log.info(
            "Ignore sendCouponUsedMessageToAlimTalk due to coupon source is membership package and membership is null {}",
            couponUsage);
        return;
      }
      this.sendWithMembershipCouponUsedMessageToAlimTalk(couponUsage);
      return;
    }
    this.sendNonMembershipCouponUsedMessageToAlimTalk(couponUsage);
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

    // 1) 기본 정보 추출
    long numberOfUses = Optional.ofNullable(coupon.getNumberOfUses()).orElse(1L);
    boolean isPermanentCoupon = Boolean.TRUE.equals(coupon.getIsPermanent());
    boolean isRecurring = Boolean.TRUE.equals(coupon.getIsRecurring())
            && coupon.getPeriodInDays() != null;
    boolean isMarketingBenefit = Boolean.TRUE.equals(coupon.getMarketingConsentBenefit());
    Integer periodInDays = coupon.getPeriodInDays(); // may be null

    // 2) 계정 조회 및 검증
    Set<Long> accountIdSet = new HashSet<>(accountIds);
    Map<Long, Account> accountMap = accountRepository.findByIdIn(accountIdSet).stream()
            .collect(Collectors.toMap(Account::getId, Function.identity()));
    if (accountMap.size() != accountIdSet.size()) {
      throw new ApiException(ErrorCode.USER_NOT_FOUND);
    }

    LocalDateTime now = LocalDateTime.now();
    List<CouponUsage> couponUsages = new ArrayList<>();
    Map<Account, CouponUsage> accountsToSendAlimTalk = new HashMap<>();

    // 3) 각 계정별 쿠폰 사용내역 생성
    accountMap.forEach((accountId, account) -> {
      LocalDateTime startDateTime;
      LocalDateTime endDateTime;
      MembershipRegistration mr = null;

      // 회원권/회사 출처일 때만 등록정보 조회
      if (source == CouponSource.MEMBERSHIP_PACKAGE || source == CouponSource.COMPANY) {
        mr = findMembershipRegistration(account);
        if (mr == null) {
          throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }
      }

      if (isRecurring || isMarketingBenefit) {
        // ▶ 정기발송 경로 (periodInDays 기준)
        // 시작일 설정
        if (source == CouponSource.MEMBERSHIP_PACKAGE) {
          startDateTime = now;
        } else if (source == CouponSource.COMPANY) {
          startDateTime = mr.getRegistrationDate();
        } else if (source == CouponSource.ADMIN_SITE) {
          startDateTime = now;
        } else {
          throw new ApiException(ErrorCode.INVALID_COUPON_SOURCE);
        }
        // 종료일 = 시작일 + periodInDays (하루 끝 시각)
        endDateTime = startDateTime
                .plusDays(periodInDays)
                .toLocalDate()
                .atTime(LocalTime.MAX);

      } else {
        // ▶ 수동발송 경로 (start/end 필드 기준)
        if (coupon.getStartedDate() == null || coupon.getEndedDate() == null) {
          throw new ApiException(ErrorCode.INVALID_COUPON_PERIOD);
        }
        startDateTime = coupon.getStartedDate();
        endDateTime = coupon.getEndedDate()
                .toLocalDate()
                .atTime(LocalTime.MAX);
      }

      // 4) CouponUsage 인스턴스 생성
      if (isPermanentCoupon) {
        // 상시할인 한 번만
        CouponUsage usage = new CouponUsage(
                coupon, account, mr,
                startDateTime, endDateTime,
                true, 0L);
        couponUsages.add(usage);
        accountsToSendAlimTalk.put(account, usage);

      } else {
        // 횟수만큼 반복 생성
        for (int i = 0; i < numberOfUses; i++) {
          CouponUsage usage = new CouponUsage(
                  coupon, account, mr,
                  startDateTime, endDateTime,
                  false, 0L);
          couponUsages.add(usage);
          if (i == 0) { // 첫 건만 알림 맵에 등록
            accountsToSendAlimTalk.put(account, usage);
          }
        }
      }
    });

    // 5) 알림톡 발송
    if (sendAlimtalk && !accountsToSendAlimTalk.isEmpty()) {
      sendCouponDeliveredMessageToAlimTalk(accountsToSendAlimTalk);
    }

    // 6) DB 저장
    return couponUsageRepository.saveAll(couponUsages);
  }


  @NonNull
  private MembershipRegistration findMembershipRegistration(@NonNull final Account account) {
    return this.membershipRegistrationRepository
        .findTopByAccountOrderByRegistrationDateDesc(account)
        .orElseThrow(
            () ->
                new ApiException(
                    ErrorCode.MEMBERSHIP_REGISTRATION_NOT_FOUND,
                    "Membership registration not found with accountId: " + account.getId()));
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
                "couponStartDate",
                coupon.getDeliveredDate().format(COUPON_DATE_FORMAT)); // No need to show time
            variables.put(
                "couponEndDate",
                coupon.getExpirationDate().format(COUPON_DATE_FORMAT)); // No need to show time
            variables.put(
                "numberOfUses",
                coupon.isPermanent() ? "상시할인" : coupon.getCoupon().getNumberOfUses() + "회");
            variables.put("CSTel", herediumProperties.getTel());
            variables.put("CSEmail", herediumProperties.getEmail());
            phonesAndMessagesToSendAlimTalk.put(account.getAccountInfo().getPhone(), variables);
          });
      this.alimTalk.sendAlimTalkWithoutTitle(
          phonesAndMessagesToSendAlimTalk, AlimTalkTemplate.COUPON_HAS_BEEN_ISSUED_V4);
    } catch (Exception e) {
      log.warn(
          "Sending message to AlimTalk failed: {}, message params: {}",
          e.getMessage(),
          phonesAndMessagesToSendAlimTalk,
          e);
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
  public void rollbackCouponDistribution(Long membershipRegistrationId) {
    this.couponUsageRepository.deleteByMembershipRegistrationId(membershipRegistrationId);
  }

  private void sendWithMembershipCouponUsedMessageToAlimTalk(final CouponUsage couponUsage) {
    log.info("Start sendWithMembershipCouponUsedMessageToAlimTalk {}", couponUsage);
    try {
      final Map<String, String> params = new HashMap<>();
      params.put("accountName", couponUsage.getAccount().getAccountInfo().getName());
      params.put("membershipName", couponUsage.getCoupon().getMembership().getName());
      params.put(
          "issuedDate",
          couponUsage.getUsedDate().format(COUPON_DATETIME_FORMAT)); // Is actually used date
      params.put("couponType", couponUsage.getCoupon().getCouponType().getDesc());
      params.put("CSTel", herediumProperties.getTel());
      params.put("CSEmail", herediumProperties.getEmail());

      this.alimTalk.sendAlimTalkWithoutTitle(
          couponUsage.getAccount().getAccountInfo().getPhone(),
          params,
          AlimTalkTemplate.WITH_MEMBERSHIP_COUPON_HAS_BEEN_USED);
    } catch (Exception e) {
      log.warn("Sending message to AlimTalk failed: ", e);
    } finally {
      log.info("End sendWithMembershipCouponUsedMessageToAlimTalk");
    }
  }

  private void sendNonMembershipCouponUsedMessageToAlimTalk(final CouponUsage couponUsage) {
    log.info("Start sendNonMembershipCouponUsedMessageToAlimTalk {}", couponUsage);
    try {
      final Map<String, String> params = new HashMap<>();
      params.put("accountName", couponUsage.getAccount().getAccountInfo().getName());
      params.put(
          "issuedDate",
          couponUsage.getUsedDate().format(COUPON_DATETIME_FORMAT)); // Is actually used date
      params.put("couponType", couponUsage.getCoupon().getCouponType().getDesc());
      params.put("CSTel", herediumProperties.getTel());
      params.put("CSEmail", herediumProperties.getEmail());

      this.alimTalk.sendAlimTalkWithoutTitle(
          couponUsage.getAccount().getAccountInfo().getPhone(),
          params,
          AlimTalkTemplate.NON_MEMBERSHIP_COUPON_HAS_BEEN_USED);
    } catch (Exception e) {
      log.warn("Sending message to AlimTalk failed: ", e);
    } finally {
      log.info("End sendNonMembershipCouponUsedMessageToAlimTalk");
    }
  }

  public CouponUsageCheckResponse checkActiveMembershipCouponUsage(
      final long membershipRegistrationId) {
    // Get the count of used coupons directly
    long usedCouponsCount =
        couponUsageRepository.countByMembershipRegistrationIdAndIsUsedTrue(
            membershipRegistrationId);

    return new CouponUsageCheckResponse(usedCouponsCount);
  }

  public void deleteAllByCompanyId(Long companyId) {
    List<CouponUsage> couponUsages = this.couponUsageRepository.findAllByCompanyId(companyId);
    this.couponUsageRepository.deleteAll(couponUsages);
  }
}
