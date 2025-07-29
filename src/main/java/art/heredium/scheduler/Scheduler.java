package art.heredium.scheduler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.entity.MembershipMileage;
import art.heredium.domain.membership.repository.MembershipMileageRepository;
import art.heredium.domain.membership.repository.MembershipRepository;
import art.heredium.service.MembershipMileageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.core.config.properties.HerediumProperties;
import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.entity.AccountInfo;
import art.heredium.domain.account.entity.NonUser;
import art.heredium.domain.account.entity.SleeperInfo;
import art.heredium.domain.account.repository.AccountInfoRepository;
import art.heredium.domain.account.repository.AccountRepository;
import art.heredium.domain.account.repository.NonUserRepository;
import art.heredium.domain.account.repository.SleeperInfoRepository;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.entity.PaymentStatus;
import art.heredium.domain.membership.repository.MembershipRegistrationRepository;
import art.heredium.domain.ticket.repository.TicketRepository;
import art.heredium.ncloud.bean.CloudMail;
import art.heredium.ncloud.bean.CloudStorage;
import art.heredium.ncloud.bean.HerediumAlimTalk;
import art.heredium.ncloud.model.EmailWithParameter;
import art.heredium.ncloud.service.sens.biz.model.kit.NCloudBizAlimTalkMessageBuilder;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkFailOverConfig;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkMessage;
import art.heredium.ncloud.type.AlimTalkTemplate;
import art.heredium.ncloud.type.MailTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class Scheduler {

  @Value("${log.config.path}")
  private String logsPath;

  private final CloudMail cloudMail;
  private final HerediumAlimTalk alimTalk;
  private final CloudStorage cloudStorage;
  private final TicketRepository ticketRepository;
  private final AccountRepository accountRepository;
  private final NonUserRepository nonUserRepository;
  private final AccountInfoRepository accountInfoRepository;
  private final SleeperInfoRepository sleeperInfoRepository;
  private final MembershipRegistrationRepository membershipRegistrationRepository;
  private final MembershipRepository membershipRepository;
  private final MembershipMileageRepository mileageRepository;
  private final MembershipMileageService mileageService;
  private final HerediumProperties herediumProperties;
  private final int accountSleepDay = 365;
  private final int accountTerminateDay = 365 * 2;
  private final int accountMailSendDay = 30;
  private final int nonUserTerminateDay = 365 * 2;
  private static final DateTimeFormatter MEMBERSHIP_REGISTER_DATETIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final Long LOG_RETENTION_PERIOD = 30L; // Days

  @Async
  @Scheduled(cron = "0 0 1 * * ?") // every  day at 1am
  @Transactional(rollbackFor = Exception.class)
  public void deleteTempFile() {
    try {
      cloudStorage.deleteTempFile();
    } catch (Exception e) {
      log.error("s3 임시 파일 삭제 스케쥴러 에러", e);
    }
    try {
      ticketRepository.updateExpire();
    } catch (Exception e) {
      log.error("티켓 기간 만료 처리 스케쥴러 에러", e);
    }
    sleepAccountSendMail();
    sleepAccount();
    terminateSendMail();
    terminateAccount();
    terminateNonUser();
  }

  @Async
  @Scheduled(cron = "0 0 0 * * ?")
  @Transactional(rollbackFor = Exception.class)
  public void removeMembershipRegistrations() {
    final List<Long> redundantMembershipRegistrationIds =
        this.membershipRegistrationRepository
            .findByPaymentStatusInAndCreatedDateBefore(
                Collections.singletonList(PaymentStatus.PENDING), LocalDateTime.now().minusDays(1))
            .stream()
            .map(MembershipRegistration::getId)
            .collect(Collectors.toList());
    this.membershipRegistrationRepository.deleteAllById(redundantMembershipRegistrationIds);
  }

//  @Async
//  @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
//  @Transactional(rollbackFor = Exception.class)
//  public void updateExpiredMemberships() {
//    try {
//      List<MembershipRegistration> expiredMemberships =
//          membershipRegistrationRepository.findByExpirationDateBeforeAndPaymentStatusNotIn(
//              LocalDateTime.now(), Arrays.asList(PaymentStatus.EXPIRED, PaymentStatus.REFUND));
//
//      expiredMemberships.forEach(
//          membership -> {
//            membership.updatePaymentStatus(PaymentStatus.EXPIRED);
//            membershipRegistrationRepository.save(membership);
//          });
//
//      if (!expiredMemberships.isEmpty()) {
//        log.info("Updated {} expired memberships", expiredMemberships.size());
//        sendExpiredMembershipNotifications(expiredMemberships);
//      }
//    } catch (Exception e) {
//      log.error("Error updating expired memberships", e);
//    }
//  }

  @Async
  @Scheduled(cron = "0 0 2 * * ?") // Every day at 2am
  @Transactional(rollbackFor = Exception.class)
  public void deleteOldLogs() {
    File[] logs = new File(logsPath).listFiles();
    if (logs == null || logs.length == 0) {
      log.info("No log file found");
      return;
    }
    final LocalDate deleteThreshold = LocalDate.now().minusDays(LOG_RETENTION_PERIOD);
    List<File> filesToDelete =
        Arrays.stream(logs)
            .filter(
                file -> {
                  try {
                    LocalDate creationDate =
                        LocalDate.from(
                            Files.readAttributes(file.toPath(), BasicFileAttributes.class)
                                .creationTime()
                                .toInstant());
                    return creationDate.isBefore(deleteThreshold);
                  } catch (IOException e) {
                    log.warn("Failed to read creation date of file: {}", file.getName());
                  }
                  return false;
                })
            .collect(Collectors.toList());
    log.info(
        "Deleting log files before {}, log files found: {}", deleteThreshold, filesToDelete.size());
    try {
      filesToDelete.forEach(File::delete);
    } catch (Exception e) {
      log.error("Failed to delete log files: {}", e.getMessage());
    }
  }


// 매월 X일 오전 10시에만 실제 발송
  @Async
  @Scheduled(cron = "0 0 10 * * *")
  @Transactional(rollbackFor = Exception.class)
  public void couponDailyAt10() {
    LocalDate today = LocalDate.now();
    int dom = today.getDayOfMonth();

    // 1) 오늘 발송 대상 쿠폰 조회
//    List<Coupon> toSend = couponRepo.findAllByIsRecurringTrueAndScheduleDay(dom);

    // 2) 각 쿠폰별 대상 회원 필터링 & 발송
//    for (Coupon c : toSend) {
//      List<User> targets = fetchTargets(c.getRecipientType(), c.isMarketingBenefit());
//      notifier.sendCoupon(c, targets);
//    }
  }

//  private List<User> fetchTargets(String recipientType, boolean marketingBenefit) {
//    switch (recipientType) {
//      case "ALL":
//        return userRepo.findAllActive();
//      case "MARKETING_ONLY":
//        return userRepo.findAllByMarketingConsentTrue();
//      // 필요시 MEMBERSHIP 등 추가 분기...
//      default:
//        return Collections.emptyList();
//    }
//  }

  // —————————————————————————————————————————————
  // 0시: 만료/승급/예정체크/마일리지소멸
  // —————————————————————————————————————————————
  /**
   * 2·3등급 만료 대상 조회 후 처리:
   *  - 2등급이면서 마일리지 ≥ 기준점수: 만료일만 1년 연장 (Retention)
   *  - 그 외(3등급 또는 2등급이지만 마일리지 부족): 1등급으로 강등
   */
  private void processExpiredTier2And3() {
    LocalDateTime now = LocalDateTime.now();

    // 1) 만료된 2·3등급 조회
    List<MembershipRegistration> expired = membershipRegistrationRepository.demoteExpiredToBasic(
            Arrays.asList(2, 3), now);
    if (expired.isEmpty()) {
      log.info("No expired tier-2/3 registrations to process");
      return;
    }

    // 2) 기본 엔티티 미리 로드
    Membership tier1 = membershipRepository.findByCode(1)
            .orElseThrow(() -> new IllegalStateException("Tier1 membership not found"));
    Membership tier2 = membershipRepository.findByCode(2)
            .orElseThrow(() -> new IllegalStateException("Membership tier 2 not found"));

    // 3) Retention(유지) 만료일: 1년 뒤 23:59:59
    LocalDateTime retentionExpiry = now.plusYears(1)
            .withHour(23).withMinute(59).withSecond(59);

    // 4) 2등급 Retention 기준 마일리지
    int retentionThreshold = tier2.getUsageThreshold();

    // 5) 분기별 대상 리스트
    List<MembershipRegistration> retentionList    = new ArrayList<>();
    List<MembershipRegistration> demotedFrom2List = new ArrayList<>();
    List<MembershipRegistration> demotedFrom3List = new ArrayList<>();

    for (MembershipRegistration reg : expired) {
      int originalCode = reg.getMembership().getCode();
      long mileageSum  = mileageRepository.sumActiveMileageByAccount(reg.getAccount().getId());

      if (originalCode == 2) {
        if (mileageSum >= retentionThreshold) {
          // → Retention: 2→2
          reg.setExpirationDate(retentionExpiry);
          reg.setLastModifiedName("SYSTEM");
          retentionList.add(reg);
          log.info("Retention extended for account {} (mileage={})",
                  reg.getAccount().getId(), mileageSum);
        } else {
          // → Demote: 2→1
          reg.setMembership(tier1);
          reg.setExpirationDate(null);
          reg.setLastModifiedName("SYSTEM");
          demotedFrom2List.add(reg);
          log.info("Demoted from 2→1 for account {}", reg.getAccount().getId());
        }
      }
      else if (originalCode == 3) {
        // → Demote: 3→1
        reg.setMembership(tier1);
        reg.setExpirationDate(null);
        reg.setLastModifiedName("SYSTEM");
        demotedFrom3List.add(reg);
        log.info("Demoted from 3→1 for account {}", reg.getAccount().getId());
      }
    }

    // 6) 일괄 저장
    membershipRegistrationRepository.saveAll(expired);

    // 7) Retention 대상 마일리지 차감
    String tier2Name = tier2.getName();
    for (MembershipRegistration reg : retentionList) {
      mileageService.createLinkedUpgradeMileage(
              reg.getAccount().getId(),
              retentionThreshold,
              tier2Name
      );
      log.info("Deducted {} mileage for retention on account {}",
              retentionThreshold, reg.getAccount().getId());
    }

    // 8) (필요시) 알림톡 발송 로직 호출
    // sendRetentionAlimTalk(retentionList);
    // sendDemoteAlimTalk(demotedFrom2List, 2);
    // sendDemoteAlimTalk(demotedFrom3List, 3);
  }

  /**
   * 매일 자정에 실행되는 멤버십 승급 로직:
   *  - 1등급 중 마일리지 ≥ 기준점수: 2등급으로 승급, 만료일 1년 연장
   *  - 승급 시 마일리지 차감 및 알림톡 예약
   */
  private void upgradeToMembership2() {
    // 1) 멤버십2 정보 조회
    Membership tier2 = membershipRepository.findByCode(2)
            .orElseThrow(() -> new IllegalStateException("Membership tier 2 not found"));
    int requiredScore = tier2.getUsageThreshold();
    String tier2Name  = tier2.getName();

    // 2) 1등급 중 승급 대상 조회
    LocalDateTime now = LocalDateTime.now();
    List<MembershipRegistration> candidates =
            membershipRegistrationRepository.findTier1WithMinMileage(requiredScore);
    if (candidates.isEmpty()) {
      log.info("No tier1 candidates for upgrade (score ≥ {})", requiredScore);
      return;
    }

    // 3) 새 만료일: 1년 뒤 같은 날 23:59:59
    LocalDateTime newExpiry = LocalDate.now().plusYears(1).atTime(23, 59, 59);

    // 4) 후보별 처리
    for (MembershipRegistration reg : candidates) {

      // (2) 실제 남은 마일리지 합계 조회
      long totalRemaining = mileageRepository
              .sumActiveMileageByAccount(reg.getAccount().getId());

      // (1) 등급·만료일 변경
      reg.setMembership(tier2);
      reg.setExpirationDate(newExpiry);
      reg.setLastModifiedName("SYSTEM");
      membershipRegistrationRepository.save(reg);

      // (2) 마일리지 차감 이벤트 기록
      mileageService.createLinkedUpgradeMileage(
              reg.getAccount().getId(),
              (int) totalRemaining,
              tier2Name
      );

      // (3) 예약 알림톡 변수 준비 및 전송
      LocalDate today = LocalDate.now();
      Map<String,String> params = new HashMap<>();
      params.put("name",           reg.getAccount().getAccountInfo().getName());
      params.put("membershipName", tier2Name);
      params.put("month",          String.valueOf(today.getMonthValue()));    // #{month} → 7
      params.put("day",            String.valueOf(today.getDayOfMonth()));     // #{day}   → 28
      params.put("CSTel",          herediumProperties.getTel());
      params.put("CSEmail",        herediumProperties.getEmail());

      //      LocalDateTime reserveTime = LocalDate.now()      // 오늘 날짜
      //                                 .atTime(10, 0);  // 오전 10시 00분

      LocalDateTime reserveTime = now.plusMinutes(11).truncatedTo(ChronoUnit.SECONDS);
      alimTalk.sendAlimTalk(
              reg.getAccount().getAccountInfo().getPhone(),
              params,
              AlimTalkTemplate.TIER_UPGRADE,
              reserveTime
      );
      log.info("Scheduled TIER_UPGRADE AlimTalk [accountId={}, reserveTime={}]",
              reg.getAccount().getId(), reserveTime);
    }
  }

  /**
   * 만료 전 3·2·1개월 알림톡 예약 (멤버십2 한정, 마일리지 부족 회원만)
   *
   *  - 오늘 기준으로 만료일까지 3·2·1개월 남은 멤버십 조회
   *  - 멤버십2(code=2)만 대상으로, 현 마일리지 < 이용실적 기준(예:70)
   *  - 남은 마일리지(필요 점수 – 현 마일리지) 값을 #{mileage} 변수로 보내기
   */
  private void scheduleTierExpiryAlimTalk() {
    LocalDateTime now = LocalDateTime.now();                      // 지금 시각(예: 2025‑07‑25T00:00)
    DateTimeFormatter isoDate = DateTimeFormatter.ISO_DATE;

    // 2등급 엔티티 + 기준 마일리지
    Membership tier2 = membershipRepository.findByCode(2)
            .orElseThrow(() -> new IllegalStateException("Tier2 not found"));
    int threshold = tier2.getUsageThreshold();                    // 예: 70점

    // “몇 개월 전” 리스트
    int[] monthsList = {3, 2, 1};

    for (int monthsBefore : monthsList) {
      // 1) ‘N개월 후 같은 날짜’ 범위 계산
      LocalDateTime targetStart = now
              .plusMonths(monthsBefore)
              .withHour(0).withMinute(0).withSecond(0).withNano(0);
      LocalDateTime targetEnd   = targetStart.plusDays(1).minusSeconds(1);
      LocalDate  targetDay      = targetStart.toLocalDate();

      // 2) DB에서 한번에 조회: 만료일 between targetStart / targetEnd,
      //    paymentStatus=ACTIVE, membership.code=2, AND mileage < threshold
      List<MembershipRegistration> toNotify =
              membershipRegistrationRepository.findTier2ExpiringWithMileageBelow(
                      targetStart, targetEnd, threshold);

      if (toNotify.isEmpty()) {
        log.info("{}-month expiry (tier2, mileage: no targets on {}",
                monthsBefore, targetDay);
        continue;
      }

      // 3) 예약 발송 시간: targetDay 오전 10시
//      LocalDateTime reserveTime = targetDay.atTime(10, 0);

      // 3) 테스트용 예약 발송 시간: 지금부터 11분 뒤
      LocalDateTime reserveTime = LocalDateTime.now()
              .plusMinutes(11)
              .truncatedTo(ChronoUnit.SECONDS);

      // 4) 알림톡 메시지 빌드
      List<NCloudBizAlimTalkMessage> batch = toNotify.stream()
              .map(reg -> {
                String name     = reg.getAccount().getAccountInfo().getName();
                String endDate  = reg.getExpirationDate().format(isoDate);
                long   used     = mileageRepository.sumActiveMileageByAccount(
                        reg.getAccount().getId());
                long   remaining = threshold - used;

                Map<String,String> vars = new HashMap<>();
                vars.put("name",           name);
                vars.put("membershipName", tier2.getName());
                vars.put("endDate",        endDate);
                vars.put("mileage",        String.valueOf(remaining));

                return new NCloudBizAlimTalkMessageBuilder()
                        .to(reg.getAccount().getAccountInfo().getPhone())
                        .title(AlimTalkTemplate.MEMBERSHIP_EXPIRY_REMINDER.getTitle())
                        .variables(vars)
                        .failOver(new NCloudBizAlimTalkFailOverConfig())
                        .build();
              })
              .collect(Collectors.toList());

      log.info("Prepared {}-month tier2 expiry reminders: {} messages",
              monthsBefore, batch.size());

      // 5) 일괄 예약 발송
      alimTalk.sendAlimTalk(
              batch,
              AlimTalkTemplate.MEMBERSHIP_EXPIRY_REMINDER,
              reserveTime
      );
      log.info("Scheduled {} expiry reminders for {} at 10:00",
              batch.size(), targetDay);
    }
  }

  @Async
  @Scheduled(cron = "0 0 0 * * ?")
  //  @Scheduled(cron = "0 * * * * *")
  @Transactional(rollbackFor = Exception.class)
  public void runMidnightTasks() {

    // 1) 만료된 2·3등급 처리 (강등/유지)
    processExpiredTier2And3();

    // 2) 1→2승급 로직 (필요시 예약 알림)
    upgradeToMembership2();

    // 3) 만료 전 3·2·1개월 알림톡 예약
    scheduleTierExpiryAlimTalk();

    // 4) 마일리지 소멸
    expireMileagePoints();
  }

  /**
   * 만료된 적립 마일리지를 찾아 소멸 처리하고, 차감 이력을 생성합니다.
   * 흐름:
   *  1) type=0(적립) 이면서 expirationDate < now 인 엔트리 조회
   *  2) 조회된 엔트리들의 type → 5(소멸완료) 로 업데이트
   *  3) 각 엔트리에 대해 type=2(소멸) 차감 이력 생성
   */
  private void expireMileagePoints() {
    LocalDateTime now = LocalDateTime.now();

    // 1) 마일리지 type=0(적립) 이면서 expirationDate가 지난 엔트리 조회
    List<MembershipMileage> toExpire =
            mileageRepository.findExpiredByTypeAndExpirationDateBefore(0, now);
    if (toExpire.isEmpty()) {
      log.info("No mileage entries to expire at {}", now);
      return;
    }

    // 2) 기존 엔트리들 type → 4 소멸완료(유효기간 경과)로 업데이트
    toExpire.forEach(m -> m.setType(4));
    mileageRepository.saveAll(toExpire);
    log.info("Marked {} mileage entries as expired", toExpire.size());

    // 3) 소멸(유효기간 경과) (type=2) 이력 추가
    toExpire.forEach(original  -> {
      mileageService.createAdjustmentMileage(
              original,
              2,
              "만료 마일리지 차감"
      );
    });

    log.info("Created {} expiry deduction entries", toExpire.size());
  }

  @Async
  @Transactional(propagation = Propagation.NEVER)
  public void sendExpiredMembershipNotifications(List<MembershipRegistration> expiredMemberships) {
    expiredMemberships.forEach(
        membership -> {
          try {
            sendMembershipExpiredMessageToAlimTalk(
                membership.getAccount().getAccountInfo().getPhone(), membership);
          } catch (Exception e) {
            log.error(
                "Failed to send AlimTalk notification for membership {}: {}",
                membership.getId(),
                e.getMessage());
          }
        });
  }

  private void sendMembershipExpiredMessageToAlimTalk(
      final String toPhone, final MembershipRegistration membership) {
    log.info("Start sendMembershipExpiredMessageToAlimTalk");
    Map<String, String> params = createMembershipExpiredParams(membership);
    try {
      this.alimTalk.sendAlimTalkWithoutTitle(
          toPhone, params, AlimTalkTemplate.MEMBERSHIP_PACKAGE_HAS_EXPIRED);
    } catch (Exception e) {
      log.warn(
          "Sending message to AlimTalk failed: {}, message params: {}", e.getMessage(), params);
    } finally {
      log.info("End sendMembershipExpiredMessageToAlimTalk");
    }
  }

  private Map<String, String> createMembershipExpiredParams(MembershipRegistration membership) {
    Map<String, String> variables = new HashMap<>();
    variables.put("accountName", membership.getAccount().getAccountInfo().getName());
    variables.put("membershipName", membership.getMembershipOrCompanyName());
    variables.put(
        "startDate", membership.getRegistrationDate().format(MEMBERSHIP_REGISTER_DATETIME_FORMAT));
    variables.put(
        "endDate", membership.getExpirationDate().format(MEMBERSHIP_REGISTER_DATETIME_FORMAT));
    variables.put("CSTel", herediumProperties.getTel());
    variables.put("CSEmail", herediumProperties.getEmail());
    return variables;
  }

  private void sleepAccountSendMail() {
    try {
      List<AccountInfo> preToSleepers =
          accountInfoRepository.findPreToSleeper(accountSleepDay - accountMailSendDay);
      if (preToSleepers.size() > 0) {
        String sleepDate =
            Constants.getNow()
                .plus(accountMailSendDay, ChronoUnit.DAYS)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        List<EmailWithParameter> emailWithParameter =
            preToSleepers.stream()
                .map(
                    account -> {
                      Map<String, String> param = new HashMap<>();
                      param.put("name", account.getName());
                      param.put("email", account.getAccount().getEmail());
                      param.put("sleepDate", sleepDate);
                      param.put("CSTel", herediumProperties.getTel());
                      param.put("CSEmail", herediumProperties.getEmail());
                      return new EmailWithParameter(account.getAccount().getEmail(), param);
                    })
                .collect(Collectors.toList());
        cloudMail.mail(emailWithParameter, MailTemplate.ACCOUNT_SLEEP);
      }
    } catch (Exception e) {
      log.error("휴면계정 전환 안내 메일 전송 스케쥴러 에러", e);
    }
  }

  private void sleepAccount() {
    try {
      List<Long> toSleeperIds = accountInfoRepository.findToSleeper(accountSleepDay);
      if (toSleeperIds.size() > 0) {
        List<Account> toSleeper = accountRepository.findAllById(toSleeperIds);
        toSleeper.forEach(
            account -> {
              SleeperInfo sleeperInfo = new SleeperInfo(account.getAccountInfo());
              account.setSleeperInfo(sleeperInfo);
            });
        accountInfoRepository.deleteAllByIdIn(toSleeperIds);
      }
    } catch (Exception e) {
      log.error("휴면계정 전환 스케쥴러 에러", e);
    }
  }

  private void terminateSendMail() {
    try {
      List<SleeperInfo> preToTerminate =
          sleeperInfoRepository.findPreToTerminate(
              accountTerminateDay - accountSleepDay - accountMailSendDay);
      if (preToTerminate.size() > 0) {
        String terminateDate =
            Constants.getNow()
                .plus(accountMailSendDay, ChronoUnit.DAYS)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd (E) HH:mm"));

        List<EmailWithParameter> emailWithParameter = new ArrayList<>();
        List<NCloudBizAlimTalkMessage> alimTalkMessages = new ArrayList<>();
        preToTerminate.forEach(
            sleeperInfo -> {
              Map<String, String> param =
                  sleeperInfo.getTerminateMailParam(herediumProperties, terminateDate);
              emailWithParameter.add(
                  new EmailWithParameter(sleeperInfo.getAccount().getEmail(), param));
              NCloudBizAlimTalkMessage message =
                  new NCloudBizAlimTalkMessageBuilder()
                      .variables(param)
                      .to(sleeperInfo.getAccount().getAccountInfo().getPhone())
                      .title(AlimTalkTemplate.ACCOUNT_NOTY_SLEEP_TERMINATE.getTitle())
                      .failOver(new NCloudBizAlimTalkFailOverConfig())
                      .build();
              alimTalkMessages.add(message);
            });
        cloudMail.mail(emailWithParameter, MailTemplate.ACCOUNT_NOTY_SLEEP_TERMINATE);
        alimTalk.sendAlimTalk(alimTalkMessages, AlimTalkTemplate.ACCOUNT_NOTY_SLEEP_TERMINATE);
      }
    } catch (Exception e) {
      log.error("회원탈퇴 안내 메일 전송 스케쥴러 에러", e);
    }
  }

  private void terminateAccount() {
    try {
      List<Account> toTerminateIds =
          accountRepository.findToTerminate(accountTerminateDay - accountSleepDay);
      if (toTerminateIds.size() > 0) {
        List<EmailWithParameter> emailWithParameter = new ArrayList<>();
        List<NCloudBizAlimTalkMessage> alimTalkMessages = new ArrayList<>();
        String terminateDate =
            Constants.getNow().format(DateTimeFormatter.ofPattern("yyyy-MM-dd (E) HH:mm"));
        toTerminateIds.forEach(
            account -> {
              Map<String, String> param =
                  account.getSleeperInfo().getTerminateMailParam(herediumProperties, terminateDate);
              emailWithParameter.add(new EmailWithParameter(account.getEmail(), param));
              NCloudBizAlimTalkMessage message =
                  new NCloudBizAlimTalkMessageBuilder()
                      .variables(param)
                      .to(account.getSleeperInfo().getPhone())
                      .title(AlimTalkTemplate.ACCOUNT_SLEEP_TERMINATE.getTitle())
                      .failOver(new NCloudBizAlimTalkFailOverConfig())
                      .build();
              alimTalkMessages.add(message);

              account.terminate();
            });
        cloudMail.mail(emailWithParameter, MailTemplate.ACCOUNT_SLEEP_TERMINATE);
        alimTalk.sendAlimTalk(alimTalkMessages, AlimTalkTemplate.ACCOUNT_SLEEP_TERMINATE);
        ticketRepository.terminateByAccount(
            toTerminateIds.stream().map(Account::getId).collect(Collectors.toList()));
      }
    } catch (Exception e) {
      log.error("회원탈퇴 스케쥴러 에러", e);
    }
  }

  private void terminateNonUser() {
    try {
      ticketRepository.terminateByNonUser(nonUserTerminateDay);
      List<NonUser> toTerminateIds = nonUserRepository.findToTerminate(nonUserTerminateDay);
      if (toTerminateIds.size() > 0) {
        toTerminateIds.forEach(NonUser::terminate);
        ticketRepository.terminateByNonUser(
            toTerminateIds.stream().map(NonUser::getId).collect(Collectors.toList()));
      }
    } catch (Exception e) {
      log.error("비회원 개인정보 삭제 스케쥴러 에러", e);
    }
  }
}
