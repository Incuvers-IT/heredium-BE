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
import java.util.stream.Collectors;

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

  @Async
  @Scheduled(cron = "0 0 0 * * ?") // Run at midnight every day
  @Transactional(rollbackFor = Exception.class)
  public void updateExpiredMemberships() {
    try {
      List<MembershipRegistration> expiredMemberships =
          membershipRegistrationRepository.findByExpirationDateBeforeAndPaymentStatusNot(
              LocalDate.now(), PaymentStatus.EXPIRED);

      expiredMemberships.forEach(
          membership -> {
            membership.updatePaymentStatus(PaymentStatus.EXPIRED);
            membershipRegistrationRepository.save(membership);
          });

      if (!expiredMemberships.isEmpty()) {
        log.info("Updated {} expired memberships", expiredMemberships.size());
        sendExpiredMembershipNotifications(expiredMemberships);
      }
    } catch (Exception e) {
      log.error("Error updating expired memberships", e);
    }
  }

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
