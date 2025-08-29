package art.heredium.scheduler;

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
import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.repository.CouponRepository;
import art.heredium.domain.membership.entity.MembershipRegistration;
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
import art.heredium.service.CouponUsageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Scheduler {

  @Value("${log.config.path}")
  private String logsPath;

  private final Environment env;
  private final CloudMail cloudMail;
  private final HerediumAlimTalk alimTalk;
  private final CloudStorage cloudStorage;
  private final TicketRepository ticketRepository;
  private final AccountRepository accountRepository;
  private final NonUserRepository nonUserRepository;
  private final AccountInfoRepository accountInfoRepository;
  private final SleeperInfoRepository sleeperInfoRepository;
  private final MembershipRegistrationRepository membershipRegistrationRepository;
  private final CouponUsageService couponUsageService;
  private final HerediumProperties herediumProperties;
  private final CouponRepository couponRepository;
  private final int accountSleepDay = 365;
  private final int accountTerminateDay = 365 * 2;
  private final int accountMailSendDay = 30;
  private final int nonUserTerminateDay = 365 * 2;
  private static final DateTimeFormatter MEMBERSHIP_REGISTER_DATETIME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final Long LOG_RETENTION_PERIOD = 30L; // Days
  private final BatchService batchService;

  @Async
  @Scheduled(cron = "0 0 1 * * ?") // every  day at 1am
  @Transactional(rollbackFor = Exception.class)
  public void deleteTempFile() {
    try {
      cloudStorage.deleteTempFile();
    } catch (Exception e) {
      log.error("s3 임시 파일 삭제 스케쥴러 에러", e);
    }
    sleepAccountSendMail();
    sleepAccount();
//    terminateSendMail();
    terminateAccount();
    terminateNonUser();
  }

//  @Async
//  @Scheduled(cron = "0 0 0 * * ?")
//  @Transactional(rollbackFor = Exception.class)
//  public void removeMembershipRegistrations() {
//    final List<Long> redundantMembershipRegistrationIds =
//        this.membershipRegistrationRepository
//            .findByPaymentStatusInAndCreatedDateBefore(
//                Collections.singletonList(PaymentStatus.PENDING), LocalDateTime.now().minusDays(1))
//            .stream()
//            .map(MembershipRegistration::getId)
//            .collect(Collectors.toList());
//    this.membershipRegistrationRepository.deleteAllById(redundantMembershipRegistrationIds);
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


  /**
   * 스케줄러 매일 01시에 동작
   * 매월 지정일 오전 10시에 정기발송(예약알림톡) 쿠폰 알림톡을 발송합니다.
   */
  @Async
  @Transactional(rollbackFor = Exception.class)
  @Scheduled(cron = "0 0 1 * * ?")
//  @Scheduled(cron = "0 * * * * *")
  public void couponDailyAt10() {
    LocalDate today = LocalDate.now();
    int dayOfMonth = today.getDayOfMonth();

    // 1) 오늘 발송 대상 쿠폰 조회
    List<Coupon> toSend = couponRepository
            .findByIsRecurringTrueAndSendDayOfMonthExcludingDefault(dayOfMonth);

    if (toSend.isEmpty()) {
      log.info(">>> couponDailyAt10: 발송 대상 쿠폰 없음 (day={})", dayOfMonth);
      return;
    }

    ObjectMapper mapper = new ObjectMapper();
    for (Coupon coupon : toSend) {

      // 2) recipientType JSON → List<Integer>
      List<Short> rawTypes = (List<Short>) coupon.getRecipientType();

      List<Integer> types = mapper.convertValue(
              rawTypes,
              new TypeReference<List<Integer>>() {}
      );

      // 3) 중복 없이 한꺼번에 모을 Set
      Set<Account> recipients = new HashSet<>();

      for (Integer t : types) {
        switch (t) {
          case 1:
            // 마케팅 동의 고객
            recipients.addAll(
                    accountRepository.findByAccountInfo_IsMarketingReceiveTrue()
            );
            break;
          case 2:
            // 마케팅 비동의 고객
            recipients.addAll(
                    accountRepository.findByAccountInfo_IsMarketingReceiveFalse()
            );
            break;

          case 6: case 7: case 8:
            // 멤버십(CN PASS 1,2,3) 가입 회원
            int membershipCode = (t == 6 ? 1 : t == 7 ? 2 : 3);
            List<MembershipRegistration> regs =
                    membershipRegistrationRepository
                            .findByMembershipCode(membershipCode);
            regs.forEach(reg -> recipients.add(reg.getAccount()));
            break;

          default:
            log.warn("Unknown recipientType {} for coupon {}", t, coupon.getId());
        }
      }

      if (recipients.isEmpty()) {
        log.info(">>> couponDailyAt10: coupon {} 대상 회원 없음", coupon.getId());
        continue;
      }

      if (env.acceptsProfiles(Profiles.of("stage", "local"))) {
        recipients.removeIf(account -> account.getId() < 5000);
      }

      // (필요하시다면) 다시 빈 집합 체크
      if (recipients.isEmpty()) {
        log.info(">>> couponDailyAt10: coupon {} (5000번 이하 계정 모두 제외)", coupon.getId());
        continue;
      }

      // 4) 예약 발송 또는 쿠폰 지급
      LocalDateTime reserveTime = today.atTime(10, 0);

      for (Account recipient : recipients) {
        // 단일 쿠폰을 한 번에 발급하도록 리스트로 포장
        List<Coupon> singleCouponList = Collections.singletonList(coupon);

        couponUsageService.distributeMembershipAndCompanyCoupons(
                recipient,         // Account
                singleCouponList,  // List<Coupon> (이번 루프의 단일 쿠폰)
                true,               // 알림톡 발송
                reserveTime,
                "SYSTEM"
        );
      }

      log.info(">>> couponDailyAt10: coupon {} scheduled to {} accounts",
              coupon.getId(), recipients.size());
    }
  }

  // 프로퍼티에서 크론/타임존 읽기(기본값: 매일 00:00, Asia/Seoul)
  @Async
  @Scheduled(cron = "${app.scheduler.run.cron:0 0 0 * * ?}", zone = "${app.scheduler.run.zone:Asia/Seoul}")
  public void runMidnightTasksTrigger() {
    log.info("[SchedulerTrigger] start");
    try {
      batchService.runAll();
    } catch (Exception e) {
      // 예외는 반드시 여기서 잡아서 로그 남기고 스케줄러 스레드가 죽지 않게!
      log.error("[SchedulerTrigger] batch failed", e);
    } finally {
      log.info("[SchedulerTrigger] end");
    }
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
