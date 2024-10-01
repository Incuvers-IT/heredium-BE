package art.heredium.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import org.apache.tika.utils.StringUtils;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.config.properties.HerediumProperties;
import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.account.repository.AccountRepository;
import art.heredium.domain.account.repository.NonUserRepository;
import art.heredium.domain.coffee.repository.CoffeeRepository;
import art.heredium.domain.common.model.ProjectInfo;
import art.heredium.domain.common.type.ProjectPriceType;
import art.heredium.domain.exhibition.repository.ExhibitionRepository;
import art.heredium.domain.log.repository.LogRepository;
import art.heredium.domain.program.repository.ProgramRepository;
import art.heredium.domain.statistics.repository.StatisticsRepository;
import art.heredium.domain.ticket.ProjectRepository;
import art.heredium.domain.ticket.ProjectRounderRepository;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.model.TicketCreateInfo;
import art.heredium.domain.ticket.model.TicketInviteCreateInfo;
import art.heredium.domain.ticket.model.dto.request.*;
import art.heredium.domain.ticket.model.dto.response.*;
import art.heredium.domain.ticket.repository.TicketRepository;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.domain.ticket.type.TicketType;
import art.heredium.ncloud.bean.CloudMail;
import art.heredium.ncloud.bean.HerediumAlimTalk;
import art.heredium.ncloud.model.EmailWithParameter;
import art.heredium.ncloud.service.sens.biz.model.kit.NCloudBizAlimTalkMessageBuilder;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkFailOverConfig;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkMessage;
import art.heredium.ncloud.type.AlimTalkTemplate;
import art.heredium.ncloud.type.MailTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TicketService {

  private final HerediumProperties herediumProperties;
  private final RestTemplate restTemplate = new RestTemplate();
  private final HerediumAlimTalk alimTalk;

  private final TicketRepository ticketRepository;
  private final LogRepository logRepository;
  private final AccountRepository accountRepository;
  private final ExhibitionRepository exhibitionRepository;
  private final ProgramRepository programRepository;
  private final CoffeeRepository coffeeRepository;
  private final StatisticsRepository statisticsRepository;
  private final NonUserRepository nonUserRepository;
  private final CloudMail cloudMail;

  @PostConstruct
  private void init() {
    restTemplate.setErrorHandler(
        new ResponseErrorHandler() {
          @Override
          public boolean hasError(ClientHttpResponse response) {
            return false;
          }

          @Override
          public void handleError(ClientHttpResponse response) {}
        });
  }

  /** 관리자 - 티켓 목록 */
  public Page<GetAdminTicketResponse> list(GetAdminTicketRequest dto, Pageable pageable) {
    return ticketRepository.search(dto, pageable).map(GetAdminTicketResponse::new);
  }

  /** 관리자 - 티켓 상세 정보 */
  public GetAdminTicketDetailResponse detailByAdmin(Long id) {
    Ticket entity = ticketRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    return new GetAdminTicketDetailResponse(entity);
  }

  /** 관리자 - 티켓 수정 */
  public boolean update(Long id, TicketStateType state) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Ticket entity = ticketRepository.findById(id).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    if (state.equals(TicketStateType.ADMIN_REFUND)) {
      if (entity.isRefund()) {
        throw new ApiException(ErrorCode.BAD_VALID, "이미 환불상태", 1);
      }
      Map<String, String> mailParam = entity.getMailParam(herediumProperties);
      if (entity.getType() == TicketType.NORMAL) {
        if (!StringUtils.isEmpty(entity.getPgId())) {
          entity.getPayment().refund(entity);
        }
        if (!StringUtils.isBlank(entity.getEmail())) {
          cloudMail.mail(entity.getEmail(), mailParam, MailTemplate.TICKET_REFUND_ADMIN);
        }
        if (!StringUtils.isBlank(entity.getPhone())) {
          alimTalk.sendAlimTalk(
              entity.getPhone(),
              entity.getMailParam(herediumProperties),
              AlimTalkTemplate.TICKET_REFUND_ADMIN);
        }
        alimTalk.cancelAlimTalk(entity.getSmsRequestId());
      } else if (entity.getType().equals(TicketType.GROUP)) {
        if (!StringUtils.isBlank(entity.getEmail())) {
          cloudMail.mail(entity.getEmail(), mailParam, MailTemplate.TICKET_REFUND_GROUP);
        }
        if (!StringUtils.isBlank(entity.getPhone())) {
          alimTalk.sendAlimTalk(
              entity.getPhone(),
              entity.getMailParam(herediumProperties),
              AlimTalkTemplate.TICKET_REFUND_GROUP);
        }
        alimTalk.cancelAlimTalk(entity.getSmsRequestId());
      }
    } else if (state.equals(TicketStateType.USED)) {
      if (!entity.isCanUse()) {
        throw new ApiException(ErrorCode.BAD_VALID, "결제상태에서만 사용완료로 변경가능", 2);
      }
      entity.updateUsedDate();
    } else {
      throw new ApiException(ErrorCode.BAD_VALID, "사용완료, 관리자환불로만 변경가능", 3);
    }

    entity.updateState(userPrincipal, state);
    ticketRepository.flush();
    logRepository.save(entity.createUpdateLog(userPrincipal.getAdmin()));
    return true;
  }

  /** 관리자 - 티켓 환불 */
  public boolean refundByAdmin(List<Long> ids) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    List<Ticket> entities = ticketRepository.findAllById(ids);
    if (ids.size() != entities.size()) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    entities.forEach(
        entity -> {
          if (entity.isRefund()) {
            throw new ApiException(ErrorCode.BAD_VALID, "이미 환불상태", 1);
          }
          entity.updateState(userPrincipal, TicketStateType.ADMIN_REFUND);
        });
    ticketRepository.flush();

    entities.forEach(
        entity -> {
          if (entity.getType() == TicketType.NORMAL && !StringUtils.isEmpty(entity.getPgId())) {
            entity.getPayment().refund(entity);
          }
        });

    List<EmailWithParameter> emailWithParameter = new ArrayList<>();
    List<NCloudBizAlimTalkMessage> alimTalkMessages = new ArrayList<>();
    entities.forEach(
        ticket -> {
          if (!StringUtils.isBlank(ticket.getEmail())) {
            Map<String, String> mailParam = ticket.getMailParam(herediumProperties);
            emailWithParameter.add(new EmailWithParameter(ticket.getEmail(), mailParam));
          }

          if (!StringUtils.isBlank(ticket.getPhone())) {
            NCloudBizAlimTalkMessage message =
                new NCloudBizAlimTalkMessageBuilder()
                    .variables(ticket.getMailParam(herediumProperties))
                    .to(ticket.getPhone())
                    .title(AlimTalkTemplate.TICKET_REFUND_ADMIN.getTitle())
                    .failOver(new NCloudBizAlimTalkFailOverConfig())
                    .build();
            alimTalkMessages.add(message);
          }
        });
    if (emailWithParameter.size() > 0) {
      cloudMail.mail(emailWithParameter, MailTemplate.TICKET_REFUND_ADMIN);
    }
    if (alimTalkMessages.size() > 0) {
      alimTalk.sendAlimTalk(alimTalkMessages, AlimTalkTemplate.TICKET_REFUND_ADMIN);
    }
    entities.forEach(entity -> alimTalk.cancelAlimTalk(entity.getSmsRequestId()));
    return true;
  }

  /** 관리자 - 단체 입장권 발급 */
  public boolean insertGroup(PostAdminTicketGroupRequest dto) {
    Account account = accountRepository.findById(dto.getAccountId()).orElse(null);
    if (account == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    ProjectRounderRepository rounder = ProjectRounderRepository.finder(dto.getKind());
    TicketCreateInfo info = rounder.toTicketCreateInfo(dto);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    Ticket ticket = new Ticket(dto, info, account, userPrincipal.getAdmin());
    ticketRepository.save(ticket);

    Map<String, String> mailParam = ticket.getMailParam(herediumProperties);
    if (!StringUtils.isBlank(ticket.getEmail())) {
      cloudMail.mail(ticket.getEmail(), mailParam, MailTemplate.TICKET_ISSUANCE_GROUP);
    }
    alimTalk.sendAlimTalk(
        ticket.getPhone(),
        ticket.getMailParam(herediumProperties),
        AlimTalkTemplate.TICKET_ISSUANCE_GROUP);
    List<String> smsRequestId =
        alimTalk.sendAlimTalk(
            ticket.getPhone(),
            ticket.getMailParam(herediumProperties),
            AlimTalkTemplate.TICKET_INFORMATION,
            ticket.getStartDate().minusDays(1).withHour(10));
    ticket.updateSmsRequestId(smsRequestId);
    ticketRepository.flush();
    return true;
  }

  /** 관리자 - 초대권 발급 */
  public boolean insertInvite(PostAdminTicketInviteRequest dto) {
    List<Account> accounts = accountRepository.findAllById(dto.getAccountIds());

    ProjectRepository rounder = ProjectRepository.finder(dto.getKind());
    TicketInviteCreateInfo info = rounder.toTicketCreateInfo(dto, accounts);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    List<Ticket> tickets = new ArrayList<>();
    accounts.forEach(
        account -> {
          Ticket ticket = new Ticket(info, account, userPrincipal.getAdmin());
          tickets.add(ticket);
        });

    ticketRepository.saveAll(tickets);

    List<EmailWithParameter> emailWithParameter = new ArrayList<>();
    List<NCloudBizAlimTalkMessage> alimTalkMessages = new ArrayList<>();
    tickets.forEach(
        ticket -> {
          Map<String, String> mailParam = ticket.getMailParam(herediumProperties);
          mailParam.put(
              "startDate", ticket.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
          emailWithParameter.add(new EmailWithParameter(ticket.getEmail(), mailParam));
          NCloudBizAlimTalkMessage message =
              new NCloudBizAlimTalkMessageBuilder()
                  .variables(ticket.getMailParam(herediumProperties))
                  .to(ticket.getPhone())
                  .title(AlimTalkTemplate.TICKET_INVITE.getTitle())
                  .failOver(new NCloudBizAlimTalkFailOverConfig())
                  .build();
          alimTalkMessages.add(message);
        });
    cloudMail.mail(emailWithParameter, MailTemplate.TICKET_INVITE);
    alimTalk.sendAlimTalk(alimTalkMessages, AlimTalkTemplate.TICKET_INVITE);
    return true;
  }

  /** 관리자 - 티켓 통계 */
  public List<GetAdminTicketStatisticsDashboardResponse> statisticsDashboard(
      Long id, TicketKindType kind) {
    GetAdminTicketStatisticsDashboardResponse total =
        statisticsRepository.dashboard(kind, id, null);
    total.setType("총 예매");

    List<GetAdminTicketStatisticsDashboardResponse> types =
        ProjectPriceType.getDefault().stream()
            .map(type -> statisticsRepository.dashboard(kind, id, type.getDesc()))
            .collect(Collectors.toList());

    GetAdminTicketStatisticsDashboardResponse etc =
        new GetAdminTicketStatisticsDashboardResponse(
            "기타",
            total.getNumber()
                - types.stream()
                    .mapToInt(GetAdminTicketStatisticsDashboardResponse::getNumber)
                    .sum(),
            total.getPrice()
                - types.stream().mapToInt(type -> Math.toIntExact(type.getPrice())).sum());

    List<GetAdminTicketStatisticsDashboardResponse> response = new ArrayList<>();
    response.add(total);
    response.addAll(types);
    response.add(etc);
    return response;
  }

  /** 티켓 상세 정보 */
  public GetUserMemberTicketDetailResponse getTicketDetail(Ticket entity) {
    ProjectRepository repository = ProjectRepository.finder(entity.getKind());
    ProjectInfo projectInfo = repository.toProjectInfo(entity.getKindId());

    return new GetUserMemberTicketDetailResponse(entity, projectInfo.getThumbnail());
  }

  /** 티켓 환불 */
  public void refund(Ticket entity) {
    if (!entity.isNormalPayment()) {
      throw new ApiException(ErrorCode.BAD_VALID, "결제상태에서만 환불가능", 1);
    }
    if (!entity.isBefore24Hour()) {
      throw new ApiException(ErrorCode.BAD_VALID, "입장 하루전까지 환불가능", 2);
    }
    entity.updateState(TicketStateType.USER_REFUND);
    ticketRepository.flush();

    if (!StringUtils.isEmpty(entity.getPgId())) {
      entity.getPayment().refund(entity);
    }

    if (!StringUtils.isBlank(entity.getEmail())) {
      cloudMail.mail(
          entity.getEmail(),
          entity.getMailParam(herediumProperties),
          MailTemplate.TICKET_REFUND_USER);
    }
    if (!StringUtils.isBlank(entity.getPhone())) {
      alimTalk.sendAlimTalk(
          entity.getPhone(),
          entity.getMailParam(herediumProperties),
          AlimTalkTemplate.TICKET_REFUND_USER);
    }
    alimTalk.cancelAlimTalk(entity.getSmsRequestId());
  }

  /** 티켓 QR 사용 */
  public PostAdminTicketQrResponse qrUse(PostTicketQrRequest dto) {

    Ticket entity = ticketRepository.findByIdAndUuid(dto.getId(), dto.getUuid());
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND, "사용할 수 없는 QR코드입니다.");
    }

    if (entity.getState() == TicketStateType.ADMIN_REFUND
        || entity.getState() == TicketStateType.USER_REFUND) {
      throw new ApiException(ErrorCode.TICKET_REFUND, "환불상태의 입장권(커피주문)입니다.");
    } else if (entity.getState() == TicketStateType.EXPIRED) {
      throw new ApiException(ErrorCode.TICKET_EXPIRED, "기간만료된 입장권(커피주문)입니다.");
    }

    ProjectRepository repository = ProjectRepository.finder(entity.getKind());
    ProjectInfo projectInfo = repository.toProjectInfo(entity.getKindId());

    if (projectInfo.getStartDate() == null || projectInfo.getEndDate() == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND, "사용할 수 없는 QR코드입니다.");
    }

    LocalDate now = Constants.getNow().toLocalDate();
    if (now.isAfter(entity.getEndDate().toLocalDate())) {
      throw new ApiException(ErrorCode.TICKET_EXPIRED, "기간만료된 입장권(커피주문)입니다.");
    }
    if (now.isBefore(entity.getStartDate().toLocalDate())) {
      throw new ApiException(
          ErrorCode.NOT_OPEN, String.format("시작되지 않은 %s입니다.", entity.getKind().getDesc()));
    }
    if (now.isAfter(projectInfo.getEndDate().toLocalDate())) {
      throw new ApiException(
          ErrorCode.THE_END, String.format("종료된 %s입니다.", entity.getKind().getDesc()));
    }
    boolean isMatch =
        dto.getAllows().stream()
            .anyMatch(
                permit ->
                    entity.getKindId().longValue() == permit.getId()
                        && entity.getKind().equals(permit.getKind()));
    if (!isMatch) {
      throw new ApiException(ErrorCode.TICKET_NOT_ALLOW, "입장, 수령 장소가 다른 입장권(커피주문)입니다.");
    }
    if (entity.getState() == TicketStateType.USED) {
      throw new ApiException(ErrorCode.TICKET_USED, "사용완료된 입장권(커피주문)입니다.");
    }
    entity.updateUsedDate();
    entity.updateState(TicketStateType.USED);

    return new PostAdminTicketQrResponse(entity);
  }

  /** 단체 관람권 신청 메일 전송 */
  public boolean groupMail(PostTicketGroupMailRequest dto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = null;
    if (authentication != null
        && authentication.isAuthenticated()
        && !authentication.getPrincipal().equals("anonymousUser")) {
      userPrincipal = (UserPrincipal) authentication.getPrincipal();
    }
    cloudMail.mail(
        herediumProperties.getEmail(),
        dto.toMap(userPrincipal, herediumProperties),
        MailTemplate.REQUEST_GROUP);
    return true;
  }

  /** 커피 사용 완료 */
  public boolean coffeeComplete(Long id) {
    Ticket entity = ticketRepository.findById(id).orElse(null);
    if (entity == null || entity.getKind() != TicketKindType.COFFEE) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    Map<String, String> param = new HashMap<>();
    param.put("name", entity.getName());
    param.put(
        "info",
        entity.getPrices().stream()
            .map(price -> String.format("%s(%d)", price.getType(), price.getNumber()))
            .collect(Collectors.joining(",")));

    if (!StringUtils.isBlank(entity.getPhone())) {
      alimTalk.sendAlimTalk(entity.getPhone(), param, AlimTalkTemplate.COFFEE_COMPLETE);
    }
    return true;
  }

  /** 티켓 qr 정보 */
  public GetUserTicketInfoResponse ticketQrInfo(String uuid) {
    Ticket entity = ticketRepository.findByUuid(uuid);
    if (entity == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    ProjectRepository repository = ProjectRepository.finder(entity.getKind());
    ProjectInfo projectInfo = repository.toProjectInfo(entity.getKindId());

    return new GetUserTicketInfoResponse(entity, projectInfo.getThumbnail());
  }
}
