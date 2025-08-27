package art.heredium.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.coupon.entity.CouponType;
import art.heredium.domain.coupon.model.dto.request.UserCouponUsageRequest;
import art.heredium.domain.coupon.model.dto.response.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import lombok.var;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.config.properties.HerediumProperties;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.repository.AccountRepository;
import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.entity.CouponSource;
import art.heredium.domain.coupon.entity.CouponUsage;
import art.heredium.domain.coupon.repository.CouponRepository;
import art.heredium.domain.coupon.repository.CouponUsageRepository;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.repository.MembershipRegistrationRepository;
import art.heredium.ncloud.bean.HerediumAlimTalk;
import art.heredium.ncloud.type.AlimTalkTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

  private static int typeOrder(Coupon c) {
    return Optional.ofNullable(c.getCouponType())
            .map(CouponType::getSortOrder)
            .orElse(Integer.MAX_VALUE);
  }

  private static final Comparator<Coupon> COUPON_ORDER =
          Comparator
                  // 1) membership_id 있는 쿠폰 먼저 (membership != null → false가 먼저 오므로 우선)
                  .comparing((Coupon c) -> c.getMembership() == null)
                  // 2) 타입 순서
                  .thenComparingInt(CouponUsageService::typeOrder)
                  // 3) 보조 정렬 (이름)
                  .thenComparing(Coupon::getName, Comparator.nullsLast(Comparator.naturalOrder()));

  public List<CouponResponseDto> getCouponsWithUsageByAccountId(Long accountId) {
    List<Coupon> coupons =
            couponUsageRepository.findDistinctCouponsByAccountIdAndIsNotDeletedIsUsed(accountId);
    List<CouponResponseDto> responseDtos = new ArrayList<>();

    coupons.sort(COUPON_ORDER);

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

  private static boolean overlaps(LocalDateTime start, LocalDateTime end,
                                  LocalDateTime from, LocalDateTime to) {
    if (start == null) return false;
    LocalDateTime e = (end != null) ? end : LocalDateTime.MAX;
    return !start.isAfter(to) && !e.isBefore(from);
  }

  public List<CouponResponseDto> getCouponsWithUsageByAccountId(
          Long accountId,
          UserCouponUsageRequest req
  ) {
      // 기간 기본값(요청이 null일 때 대비)
      LocalDateTime now  = LocalDateTime.now();
      LocalDateTime from = req.getStartDate() != null ? req.getStartDate() : now.minusMonths(1).with(LocalTime.MIN);
      LocalDateTime to   = req.getEndDate()   != null ? req.getEndDate()   : now.with(LocalTime.MAX);

      boolean tabAvailable = req.isAvailableTab();
      boolean tabUsed      = req.isUsedTab();
      boolean tabTotal     = !tabAvailable && !tabUsed;

      List<Coupon> coupons =
              couponUsageRepository.findDistinctCouponsByAccountIdAndIsNotDeleted(accountId);

      coupons.sort(COUPON_ORDER);

      List<CouponResponseDto> result = new ArrayList<>(coupons.size());

      for (Coupon coupon : coupons) {
          // 원본 조회
          List<CouponUsage> usedRaw =
                  couponUsageRepository.findByAccountIdAndCouponIdAndIsUsedTrue(accountId, coupon.getId());

          List<CouponUsage> unusedRaw =
                  couponUsageRepository.findUnusedOrPermanentCoupons(accountId, coupon.getId())
                          .stream()
                          .sorted(Comparator.comparing(
                                  CouponUsage::getExpirationDate,
                                  Comparator.nullsLast(Comparator.naturalOrder())))
                          .collect(Collectors.toList());

          // 탭/기간 필터

          // 사용 탭: usedDate 기준
          List<CouponUsage> usedFiltered = usedRaw.stream()
                  .filter(cu -> !cu.isPermanent())
                  .filter(cu -> overlaps(cu.getDeliveredDate(), cu.getExpirationDate(), from, to))
                  .collect(Collectors.toList());

          // 보유/전체: deliveredDate(발급일) 기준
          List<CouponUsage> unusedFiltered = unusedRaw.stream()
                  .filter(cu -> {
                      LocalDateTime delivered = cu.getDeliveredDate();
                      if (delivered == null) return false;

                      // 만료일 값이 있으면 그대로, 없으면 무한대
                      LocalDateTime expiration = Optional.ofNullable(cu.getExpirationDate())
                              .orElse(LocalDateTime.MAX);

                      // [delivered, expiration] 과 [from, to] 겹치면 포함
                      return !delivered.isAfter(to) && !expiration.isBefore(from);
                  })
                  .sorted(Comparator.comparing(
                          CouponUsage::getExpirationDate, Comparator.nullsLast(Comparator.naturalOrder())))
                  .collect(Collectors.toList());

          // 탭에 맞춰 한쪽 비우기
          if (tabAvailable) {
            if (unusedFiltered.isEmpty()) continue;
            usedFiltered = Collections.emptyList();
          } else if (tabUsed) {
            if (usedFiltered.isEmpty()) continue;
            unusedFiltered = Collections.emptyList();
          } else { // total
            if (usedFiltered.isEmpty() && unusedFiltered.isEmpty()) continue;
          }

          result.add(new CouponResponseDto(coupon, usedFiltered, unusedFiltered));
      }

      return result;
  }
    public CouponUsagePage getCouponsWithUsagePage(Long accountId,
                                                   UserCouponUsageRequest req,
                                                   Pageable pageable) {
      // 1) 테이블(페이지네이션)용: 탭/기간이 적용된 데이터
      List<CouponResponseDto> pageRows = getCouponsWithUsageByAccountId(accountId, req);

      // 1.5) 요약 합계용: 탭/기간 무관한 전역 데이터
      List<CouponResponseDto> globalRows = getCouponsWithUsageByAccountId(accountId);

      // 2) 합계 계산 (전역 기준)
      final LocalDateTime now = LocalDateTime.now();
      final LocalDateTime in30d = now.plusDays(30);

      long totalCoupons = globalRows.stream()
              .mapToLong(row -> {
                List<CouponUsageDto> unused = row.getUnusedCoupons();
                if (unused == null) return 0L;

                return unused.stream()
                        .filter(cu -> !cu.isExpired())
                        .count();
              })
              .sum();

      long expiringCoupons = globalRows.stream()
              .mapToLong(row -> {
                List<CouponUsageDto> unused = row.getUnusedCoupons();
                if (unused == null) return 0L;
                return unused.stream()
                        .filter(cu -> !cu.isPermanent())
                        .filter(cu -> {
                          var exp = cu.getExpirationDate();
                          return exp != null && (exp.isAfter(now) || exp.isEqual(now))
                                  && (exp.isBefore(in30d) || exp.isEqual(in30d));
                        })
                        .count();
              })
              .sum();

      // 3) 메모리 슬라이스 페이징 (페이지용 리스트 기준)
      int totalElements = pageRows.size();
      int size   = pageable.getPageSize();
      int number = pageable.getPageNumber();
      int from   = Math.min(number * size, totalElements);
      int to     = Math.min(from + size, totalElements);
      List<CouponResponseDto> content = pageRows.subList(from, to);
      int totalPages = (int) Math.ceil(totalElements / (double) size);

      // 4) 응답
      CouponUsagePage resp = new CouponUsagePage();
      resp.setContent(content);
      resp.setTotalElements(totalElements);
      resp.setTotalPages(totalPages);
      resp.setNumber(number);
      resp.setSize(size);
      resp.setFirst(number == 0);
      resp.setLast(totalPages == 0 || number >= totalPages - 1);

      // 전역 합계 세팅 (탭/기간과 독립)
      resp.setTotalCoupons(totalCoupons);
      resp.setExpiringCoupons(expiringCoupons);
      return resp;
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
    this.assignCouponToAccounts(coupon, accountIds, CouponSource.ADMIN_SITE, true, null, null);
  }

  /**
   * @param account       쿠폰을 받을 계정
   * @param coupons       발급할 쿠폰 리스트
   * @param sendAlimtalk  알림톡 발송 여부
   * @param reserveTime   null 이면 즉시 발송, null이 아니면 해당 시각으로 예약 발송
   */
  @Transactional(rollbackFor = Exception.class)
  public List<CouponUsage> distributeMembershipAndCompanyCoupons(
          @NonNull Account account,
          @NonNull List<Coupon> coupons,
          boolean sendAlimtalk,
          LocalDateTime reserveTime,
          String name
  ) {
    List<CouponUsage> allSaved = new ArrayList<>();
    Long accountId = account.getId();

    for (Coupon coupon : coupons) {
      List<CouponUsage> saved = assignCouponToAccounts(
              coupon,
              Collections.singletonList(accountId),
              coupon.getFromSource(),
              sendAlimtalk,
              reserveTime,
              name
      );
      allSaved.addAll(saved);
    }
    return allSaved;
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
//    if (couponUsage.getCoupon().getFromSource() == CouponSource.MEMBERSHIP_PACKAGE) {
//      if (couponUsage.getCoupon().getMembership() == null) {
//        log.info(
//                "Ignore sendCouponUsedMessageToAlimTalk due to coupon source is membership package and membership is null {}",
//                couponUsage);
//        return;
//      }
//      this.sendWithMembershipCouponUsedMessageToAlimTalk(couponUsage);
//      return;
//    }
//    this.sendNonMembershipCouponUsedMessageToAlimTalk(couponUsage);
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

  /**
   * @param coupon          발급할 쿠폰
   * @param accountIds      발급 대상 계정 ID 목록
   * @param source          쿠폰 출처
   * @param sendAlimtalk    알림톡 발송 여부
   * @param reserveTime     null → 즉시, 아니면 예약 시각
   */
  private List<CouponUsage> assignCouponToAccounts(
          final Coupon coupon,
          @NonNull final List<Long> accountIds,
          @NonNull final CouponSource source,
          final boolean sendAlimtalk,
          LocalDateTime reserveTime,
          String name
  ) {

    final String actorNameFinal;
    if (name != null) {
      actorNameFinal = name;
    } else {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        String extractedName = principal.getName();
        // DB 제약 길이 방어
        actorNameFinal = (extractedName != null && extractedName.length() > 10)
                ? extractedName.substring(0, 10)
                : extractedName;
      } else {
        actorNameFinal = "SYSTEM";
      }
    }

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
                coupon, account, mr, startDateTime, endDateTime, true, 0L, actorNameFinal
        );

        couponUsages.add(usage);
        accountsToSendAlimTalk.put(account, usage);

      } else {
        // 횟수만큼 반복 생성
        for (int i = 0; i < numberOfUses; i++) {
          CouponUsage usage = new CouponUsage(
                  coupon, account, mr, startDateTime, endDateTime, false, 0L, actorNameFinal
          );

          couponUsages.add(usage);
          if (i == 0) { // 첫 건만 알림 맵에 등록
            accountsToSendAlimTalk.put(account, usage);
          }
        }
      }
    });

    // 6) DB 저장 (먼저 저장)
    List<CouponUsage> saved = couponUsageRepository.saveAll(couponUsages);

    // 5’) 커밋 이후 발송 예약
    if (sendAlimtalk && !accountsToSendAlimTalk.isEmpty()) {
      runAfterCommit(() ->
              sendCouponDeliveredMessageToAlimTalk(accountsToSendAlimTalk, reserveTime)
      );
    }

    // 6) DB 저장
    return saved;

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
          final Map<Account, CouponUsage> accountsToSendAlimTalk, LocalDateTime reserveTime) {
    log.info("Start sendCouponDeliveredMessageToAlimTalk {}", accountsToSendAlimTalk);

    Map<String, Map<String, String>> phonesAndMessagesToSendAlimTalk = new HashMap<>();
    accountsToSendAlimTalk.forEach((account, coupon) -> {
      Map<String, String> variables = new HashMap<>();
      variables.put("accountName", account.getAccountInfo().getName());
      variables.put("couponType",  coupon.getCoupon().getCouponType().getDesc());
      variables.put("couponName",  coupon.getCoupon().getName());
      variables.put("discountPercent",
              coupon.getCoupon().getDiscountPercent() != 100
                      ? coupon.getCoupon().getDiscountPercent() + "%"
                      : "무료"
      );
      variables.put("couponStartDate",
              coupon.getDeliveredDate().format(COUPON_DATE_FORMAT)
      );
      variables.put("couponEndDate",
              coupon.getExpirationDate().format(COUPON_DATE_FORMAT)
      );
      variables.put("numberOfUses",
              coupon.isPermanent()
                      ? "상시할인"
                      : coupon.getCoupon().getNumberOfUses() + "회"
      );
      variables.put("CSTel",   herediumProperties.getTel());
      variables.put("CSEmail", herediumProperties.getEmail());
      phonesAndMessagesToSendAlimTalk.put(
              account.getAccountInfo().getPhone(),
              variables
      );
    });

    try {
      // single call handles both immediate (reserveTime==null) and scheduled
      alimTalk.sendAlimTalkWithoutTitle(
              phonesAndMessagesToSendAlimTalk,
              AlimTalkTemplate.COUPON_HAS_BEEN_ISSUED_V4,
              reserveTime
      );
    } catch (Exception e) {
      log.warn("Sending message to AlimTalk failed: {}, params: {}",
              e.getMessage(), phonesAndMessagesToSendAlimTalk, e);
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

  /**
   * 멤버십 전용 쿠폰 발급
   */
  @Transactional(rollbackFor = Exception.class)
  public List<CouponUsage> distributeCouponsForMembership(
          @NonNull Account account,
          @NonNull MembershipRegistration registration,
          boolean sendAlimtalk,
          LocalDateTime reserveTime
  ) {

    // 0) 기존 멤버십 쿠폰 삭제
    couponUsageRepository.deleteByAccountIdAndMembershipRegistrationIdIsNotNull(account.getId());

    // 1) 해당 멤버십에 연결된 쿠폰 조회
    List<Coupon> coupons = couponRepository
            .findByMembershipIdAndIsDeletedFalse(registration.getMembership().getId());

    if (coupons.isEmpty()) {
      return Collections.emptyList();
    }

    LocalDateTime forcedEnd = registration.getExpirationDate();
    if (forcedEnd == null) {
      forcedEnd = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
    }

    // 2) 각 쿠폰마다 계정에 발급
    List<CouponUsage> usages = new ArrayList<>();
    for (Coupon coupon : coupons) {
      usages.addAll(
              assignCouponToAccountsWithFixedPeriod(
                      coupon,
                      Collections.singletonList(account.getId()),
                      CouponSource.MEMBERSHIP_PACKAGE,
                      sendAlimtalk,
                      reserveTime,
                      registration.getRegistrationDate(),
                      forcedEnd,
                      registration
              )
      );
    }

    // 3) DB 저장
    return couponUsageRepository.saveAll(usages);
  }

  /**
   * 고정 기간 발급 (forcedStart/forcedEnd 적용)
   */
  private List<CouponUsage> assignCouponToAccountsWithFixedPeriod(
          Coupon coupon,
          List<Long> accountIds,
          CouponSource source,
          boolean sendAlimtalk,
          LocalDateTime reserveTime,
          LocalDateTime forcedStart,
          LocalDateTime forcedEnd,
          MembershipRegistration registration
  ) {
    long numberOfUses = Optional.ofNullable(coupon.getNumberOfUses()).orElse(1L);
    boolean isPermanent = Boolean.TRUE.equals(coupon.getIsPermanent());

    // 계정 조회 및 검증
    Map<Long, Account> accountMap = accountRepository.findByIdIn(new HashSet<>(accountIds))
            .stream().collect(Collectors.toMap(Account::getId, Function.identity()));
    if (accountMap.size() != accountIds.size()) {
      throw new ApiException(ErrorCode.USER_NOT_FOUND);
    }

    List<CouponUsage> result = new ArrayList<>();
    Map<Account, CouponUsage> talkMap = new java.util.HashMap<>();

    // 발급 로직
    for (Account acct : accountMap.values()) {
      if (isPermanent) {
        CouponUsage u = new CouponUsage(
                coupon, acct, registration,
                forcedStart, forcedEnd,
                true, 0L, "SYSTEM"
        );
        result.add(u);
        talkMap.put(acct, u);
      } else {
        for (int i = 0; i < numberOfUses; i++) {
          CouponUsage u = new CouponUsage(
                  coupon, acct, registration,
                  forcedStart, forcedEnd,
                  false, 0L, "SYSTEM"
          );
          result.add(u);
          if (i == 0) talkMap.put(acct, u);
        }
      }
    }

    if (sendAlimtalk && !talkMap.isEmpty()) {
      runAfterCommit(() ->
              sendCouponDeliveredMessageToAlimTalk(talkMap, reserveTime)
      );
    }

    return result;
  }

  public boolean rollbackUsageIfPresent(String couponUsageUuid) {
    // 1) 존재 확인
    Optional<CouponUsage> usageOpt = couponUsageRepository.findByUuid(couponUsageUuid);
    if (!usageOpt.isPresent()) return false;

    // 2) 실제 롤백 (내부에서 중복방지도 idempotent하게)
    rollbackCouponUsage(couponUsageUuid);
    return true;
  }

  private void runAfterCommit(Runnable task) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override public void afterCommit() { task.run(); }
      });
    } else {
      // 트랜잭션 밖이면 즉시 실행
      task.run();
    }
  }
}
