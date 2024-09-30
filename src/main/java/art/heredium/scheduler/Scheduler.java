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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Scheduler {

    private final CloudMail cloudMail;
    private final HerediumAlimTalk alimTalk;
    private final CloudStorage cloudStorage;
    private final TicketRepository ticketRepository;
    private final AccountRepository accountRepository;
    private final NonUserRepository nonUserRepository;
    private final AccountInfoRepository accountInfoRepository;
    private final SleeperInfoRepository sleeperInfoRepository;
    private final HerediumProperties herediumProperties;
    private final int accountSleepDay = 365;
    private final int accountTerminateDay = 365 * 2;
    private final int accountMailSendDay = 30;
    private final int nonUserTerminateDay = 365 * 2;

    @Async
    @Scheduled(cron = "0 0 1 * * ?") //every  day at 1am
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

    private void sleepAccountSendMail() {
        try {
            List<AccountInfo> preToSleepers = accountInfoRepository.findPreToSleeper(accountSleepDay - accountMailSendDay);
            if (preToSleepers.size() > 0) {
                String sleepDate = Constants.getNow().plus(accountMailSendDay, ChronoUnit.DAYS).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                List<EmailWithParameter> emailWithParameter = preToSleepers.stream().map(account -> {
                    Map<String, String> param = new HashMap<>();
                    param.put("name", account.getName());
                    param.put("email", account.getAccount().getEmail());
                    param.put("sleepDate", sleepDate);
                    param.put("CSTel", herediumProperties.getTel());
                    param.put("CSEmail", herediumProperties.getEmail());
                    return new EmailWithParameter(account.getAccount().getEmail(), param);
                }).collect(Collectors.toList());
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
                toSleeper.forEach(account -> {
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
            List<SleeperInfo> preToTerminate = sleeperInfoRepository.findPreToTerminate(accountTerminateDay - accountSleepDay - accountMailSendDay);
            if (preToTerminate.size() > 0) {
                String terminateDate = Constants.getNow().plus(accountMailSendDay, ChronoUnit.DAYS).format(DateTimeFormatter.ofPattern("yyyy-MM-dd (E) HH:mm"));

                List<EmailWithParameter> emailWithParameter = new ArrayList<>();
                List<NCloudBizAlimTalkMessage> alimTalkMessages = new ArrayList<>();
                preToTerminate.forEach(sleeperInfo -> {
                    Map<String, String> param = sleeperInfo.getTerminateMailParam(herediumProperties, terminateDate);
                    emailWithParameter.add(new EmailWithParameter(sleeperInfo.getAccount().getEmail(), param));
                    NCloudBizAlimTalkMessage message = new NCloudBizAlimTalkMessageBuilder()
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
            List<Account> toTerminateIds = accountRepository.findToTerminate(accountTerminateDay - accountSleepDay);
            if (toTerminateIds.size() > 0) {
                List<EmailWithParameter> emailWithParameter = new ArrayList<>();
                List<NCloudBizAlimTalkMessage> alimTalkMessages = new ArrayList<>();
                String terminateDate = Constants.getNow().format(DateTimeFormatter.ofPattern("yyyy-MM-dd (E) HH:mm"));
                toTerminateIds.forEach(account -> {
                    Map<String, String> param = account.getSleeperInfo().getTerminateMailParam(herediumProperties, terminateDate);
                    emailWithParameter.add(new EmailWithParameter(account.getEmail(), param));
                    NCloudBizAlimTalkMessage message = new NCloudBizAlimTalkMessageBuilder()
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
                ticketRepository.terminateByAccount(toTerminateIds.stream().map(Account::getId).collect(Collectors.toList()));
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
                ticketRepository.terminateByNonUser(toTerminateIds.stream().map(NonUser::getId).collect(Collectors.toList()));
            }
        } catch (Exception e) {
            log.error("비회원 개인정보 삭제 스케쥴러 에러", e);
        }
    }
}
