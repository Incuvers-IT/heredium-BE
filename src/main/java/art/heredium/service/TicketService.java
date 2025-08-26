package art.heredium.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import art.heredium.domain.membership.entity.MembershipMileage;
import lombok.AllArgsConstructor;
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
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.repository.MembershipRegistrationRepository;
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
import lombok.Getter;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TicketService {

  private final CouponUsageService couponUsageService;
  private final HerediumProperties herediumProperties;
  private final RestTemplate restTemplate = new RestTemplate();
  private final HerediumAlimTalk alimTalk;

  private final MembershipMileageService mileageService;
  private final TicketRepository ticketRepository;
  private final LogRepository logRepository;
  private final AccountRepository accountRepository;
  private final ExhibitionRepository exhibitionRepository;
  private final ProgramRepository programRepository;
  private final CoffeeRepository coffeeRepository;
  private final StatisticsRepository statisticsRepository;
  private final NonUserRepository nonUserRepository;
  private final CloudMail cloudMail;
  private final MembershipRegistrationRepository membershipRegistrationRepository;

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

  @Getter
  @AllArgsConstructor
  static class RefundContext {
    private final Map<String,String> params;
    private final EmailWithParameter email;                  // null 가능
    private final MailTemplate mailTemplate;                 // null 가능
    private final NCloudBizAlimTalkMessage alimtalkMessage;  // null 가능
    private final AlimTalkTemplate alimtalkTemplate;         // null 가능
    private final List<String> smsRequestIdsToCancel;        // 빈 리스트 가능
    private final Ticket ticket;                             // 필요 시 참조
  }

  /** 관리자 - 티켓 목록 */
  public Page<GetAdminTicketResponse> list(GetAdminTicketRequest dto, Pageable pageable) {
    Page<Ticket> ticketPage = ticketRepository.search(dto, pageable);

    List<Long> accountIds =
        ticketPage.getContent().stream()
            .map(Ticket::getAccount)
            .filter(Objects::nonNull)
            .map(Account::getId)
            .distinct()
            .collect(Collectors.toList());

    final Map<Long, MembershipRegistration> membershipRegistrations;
    if (!accountIds.isEmpty() && (dto.getHasMembership() == null || dto.getHasMembership())) {
      membershipRegistrations =
          membershipRegistrationRepository.findLatestForAccounts(accountIds).stream()
              .collect(Collectors.toMap(mr -> mr.getAccount().getId(), Function.identity()));
    } else {
      membershipRegistrations = Collections.emptyMap();
    }

    return ticketPage.map(
        ticket -> {
          MembershipRegistration membershipRegistration = null;
          if (ticket.getAccount() != null) {
            membershipRegistration = membershipRegistrations.get(ticket.getAccount().getId());
          }
          return new GetAdminTicketResponse(ticket, membershipRegistration);
        });
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
      // 공통 코어 + 단건 전송
      RefundContext ctx = doAdminRefundCore(entity, "관리자 환불", userPrincipal);
      sendNotificationsSingle(ctx);
      return true;

    } else if (state.equals(TicketStateType.USED)) {
      if (!entity.isCanUse()) {
        throw new ApiException(ErrorCode.BAD_VALID, "결제상태에서만 사용완료로 변경가능", 2);
      }
      entity.updateUsedDate();

      // ── [ADD] 멤버십3(미성년자) 적립 스킵 ─────────────────────────────
      final int MINOR_MEMBERSHIP_CODE = 3;

      // 기존 사용완료 적립 로직 유지
      try {
        final Account acc = entity.getAccount();
        if (acc != null && !StringUtils.isBlank(entity.getEmail())) {

          final LocalDateTime now = LocalDateTime.now();
          boolean minorActive = membershipRegistrationRepository
                  .findLatestForAccount(acc.getId())
                  .map(reg -> {
                    boolean active = reg.getExpirationDate() == null || reg.getExpirationDate().isAfter(now);
                    return active
                            && reg.getMembership() != null
                            && reg.getMembership().getCode() == MINOR_MEMBERSHIP_CODE;
                  })
                  .orElse(false);

          if (minorActive) {
            if (log.isDebugEnabled()) {
              log.debug("[AdminTicketUpdate] skip accrual for minor (accountId={}, ticketId={})",
                      acc.getId(), entity.getId());
            }
          } else {
            long origin = Optional.ofNullable(entity.getOriginPrice()).orElse(0L);
            long discount = Optional.ofNullable(entity.getCouponDiscountAmount()).orElse(0L);
            long net = Math.max(0L, origin - discount);
            int points = (int) Math.floorDiv(net, 1000L);

            if (points > 0) {
              int paymentAmount = (net > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) net;
              MembershipMileage saved = mileageService.earnFromTicket(
                      entity.getAccount().getId(),
                      points,
                      0,
                      paymentAmount,
                      "티켓 사용완료 적립",
                      entity
              );
            }
          }
        }
      } catch (Exception e) {
        log.error("Earn mileage on USED failed (ticketId={})", entity.getId(), e);
      }

      // USED 상태 업데이트/로그 (기존과 동일)
      entity.updateState(userPrincipal, state);
      ticketRepository.flush();
      logRepository.save(entity.createUpdateLog(userPrincipal.getAdmin()));
      return true;

    } else {
      throw new ApiException(ErrorCode.BAD_VALID, "사용완료, 관리자환불로만 변경가능", 3);
    }
  }

  /** 관리자 - 티켓 환불(선택항목 일괄) */
  public boolean refundByAdmin(List<Long> ids) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

    List<Ticket> entities = ticketRepository.findAllById(ids);
    if (ids.size() != entities.size()) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    // 템플릿별로 묶기
    Map<MailTemplate, List<EmailWithParameter>> emailsByTpl = new HashMap<>();
    Map<AlimTalkTemplate, List<NCloudBizAlimTalkMessage>> talksByTpl = new HashMap<>();
    List<String> allSmsToCancel = new ArrayList<>();

    for (Ticket e : entities) {
      RefundContext ctx = doAdminRefundCore(e, "관리자 환불(일괄)", userPrincipal);

      if (ctx.getEmail() != null && ctx.getMailTemplate() != null) {
        emailsByTpl.computeIfAbsent(ctx.getMailTemplate(), k -> new ArrayList<>()).add(ctx.getEmail());
      }
      if (ctx.getAlimtalkMessage() != null && ctx.getAlimtalkTemplate() != null) {
        talksByTpl.computeIfAbsent(ctx.getAlimtalkTemplate(), k -> new ArrayList<>()).add(ctx.getAlimtalkMessage());
      }
      if (ctx.getSmsRequestIdsToCancel() != null) {
        allSmsToCancel.addAll(ctx.getSmsRequestIdsToCancel());
      }
    }

    // 일괄 발송
    sendNotificationsBatch(emailsByTpl, talksByTpl, allSmsToCancel);
    return true;
  }

  /** 공통 코어: 환불 처리(단건 기준) + 전송 페이로드 생성 */
  private RefundContext doAdminRefundCore(Ticket entity, String reason, UserPrincipal actor) {
    if (entity.isRefund()) {
      throw new ApiException(ErrorCode.BAD_VALID, "이미 환불상태", 1);
    }

    // PG 환불
    if (entity.getType() == TicketType.NORMAL && !StringUtils.isEmpty(entity.getPgId())) {
      entity.getPayment().refund(entity);
    }

    // 쿠폰 롤백 (존재 시에만, idempotent)
    if (entity.getCouponUuid() != null && Boolean.FALSE.equals(entity.getIsCouponAlreadyRefund())) {
      try {
        boolean rolledBack = couponUsageService.rollbackUsageIfPresent(entity.getCouponUuid());
        if (!rolledBack) {
          log.warn("Coupon usage not found to rollback. ticketId={}, couponUuid={}",
                  entity.getId(), entity.getCouponUuid());
        }
      } catch (Exception ex) {
        log.error("Coupon rollback failed. ticketId={}, couponUuid={}",
                entity.getId(), entity.getCouponUuid(), ex);
        // 정책: 여기서 실패해도 환불 프로세스 계속 진행할지 결정. 보통 계속 진행 권장.
      } finally {
        // 재시도 루프 방지
        entity.setCouponAlreadyRefund(true);
        ticketRepository.save(entity);
      }
    }

    // 마일리지 취소 & (필요시) 멤버십 강등
    try {
      mileageService.refundTicketMileageAndMaybeDemote(entity, reason);
    } catch (Exception e) {
      log.error("Refund mileage failed for ticketId={}", entity.getId(), e);
      // 정책에 따라 롤백/무시 결정. 현재는 진행.
    }

    // 상태 변경 + 로깅
    if (actor != null) {
      entity.updateState(actor, TicketStateType.ADMIN_REFUND);
    } else {
      entity.updateState(TicketStateType.ADMIN_REFUND);
    }
    ticketRepository.flush();
    logRepository.save(entity.createUpdateLog(actor != null ? actor.getAdmin() : null));

    // 메일/알림 페이로드
    Map<String, String> params = entity.getMailParam(herediumProperties);

    MailTemplate mailTpl = null;
    if (!StringUtils.isBlank(entity.getEmail())) {
      mailTpl = (entity.getType() == TicketType.GROUP)
              ? MailTemplate.TICKET_REFUND_GROUP
              : MailTemplate.TICKET_REFUND_ADMIN;
    }
    EmailWithParameter emailParam =
            (mailTpl != null) ? new EmailWithParameter(entity.getEmail(), params) : null;

    AlimTalkTemplate talkTpl = null;
    if (!StringUtils.isBlank(entity.getPhone())) {
      talkTpl = (entity.getType() == TicketType.GROUP)
              ? AlimTalkTemplate.TICKET_REFUND_GROUP
              : AlimTalkTemplate.TICKET_REFUND_ADMIN;
    }
    NCloudBizAlimTalkMessage alimMsg = null;
    if (talkTpl != null) {
      alimMsg = new NCloudBizAlimTalkMessageBuilder()
              .variables(params)
              .to(entity.getPhone())
              .title(talkTpl.getTitle())
              .failOver(new NCloudBizAlimTalkFailOverConfig())
              .build();
    }

    List<String> smsToCancel = entity.getSmsRequestId();

    return new RefundContext(params, emailParam, mailTpl, alimMsg, talkTpl, smsToCancel, entity);
  }

  /** 단건 전송 */
  private void sendNotificationsSingle(RefundContext ctx) {
    // 메일
    if (ctx.getMailTemplate() != null) {
      String email = (ctx.getTicket() != null) ? ctx.getTicket().getEmail() : null;
      if (!StringUtils.isBlank(email)) {
        try {
          cloudMail.mail(email, ctx.getParams(), ctx.getMailTemplate());
        } catch (Exception ex) {
          log.error("Single mail send failed. ticketId={}, email={}, tpl={}",
                  (ctx.getTicket() != null ? ctx.getTicket().getId() : null), email, ctx.getMailTemplate(), ex);
        }
      }
    }

    // 알림톡
    if (ctx.getAlimtalkTemplate() != null) {
      String phone = (ctx.getTicket() != null) ? ctx.getTicket().getPhone() : null;
      if (!StringUtils.isBlank(phone)) {
        try {
          // 단건은 템플릿/파라미터 방식 사용
          alimTalk.sendAlimTalk(phone, ctx.getParams(), ctx.getAlimtalkTemplate());
        } catch (Exception ex) {
          log.error("Single alimtalk send failed. ticketId={}, phone={}, tpl={}",
                  (ctx.getTicket() != null ? ctx.getTicket().getId() : null), phone, ctx.getAlimtalkTemplate(), ex);
        }
      }
    }

    // 예약 알림톡 취소
    List<String> smsIds = ctx.getSmsRequestIdsToCancel();
    if (smsIds != null && !smsIds.isEmpty()) {
      try {
        // 중복 방지
        List<String> distinctIds = new ArrayList<>(new LinkedHashSet<>(smsIds));
        alimTalk.cancelAlimTalk(distinctIds);
      } catch (Exception ex) {
        log.error("Single alimtalk-cancel failed. ticketId={}, smsIds={}",
                (ctx.getTicket() != null ? ctx.getTicket().getId() : null), smsIds, ex);
      }
    }
  }

  /** 배치 전송: 템플릿별로 묶어서 일괄 발송 */
  private void sendNotificationsBatch(
          Map<MailTemplate, List<EmailWithParameter>> emailsByTpl,
          Map<AlimTalkTemplate, List<NCloudBizAlimTalkMessage>> talksByTpl,
          List<String> allSmsToCancel
  ) {
    // 메일 템플릿별 배치 발송
    if (emailsByTpl != null && !emailsByTpl.isEmpty()) {
      emailsByTpl.forEach((tpl, list) -> {
        if (tpl != null && list != null && !list.isEmpty()) {
          try {
            cloudMail.mail(list, tpl);
          } catch (Exception ex) {
            log.error("Batch mail send failed. tpl={}, size={}", tpl, list.size(), ex);
          }
        }
      });
    }

    // 알림톡 템플릿별 배치 발송
    if (talksByTpl != null && !talksByTpl.isEmpty()) {
      talksByTpl.forEach((tpl, list) -> {
        if (tpl != null && list != null && !list.isEmpty()) {
          try {
            alimTalk.sendAlimTalk(list, tpl);
          } catch (Exception ex) {
            log.error("Batch alimtalk send failed. tpl={}, size={}", tpl, list.size(), ex);
          }
        }
      });
    }

    // 예약 알림톡 전체 취소 (중복 제거)
    if (allSmsToCancel != null && !allSmsToCancel.isEmpty()) {
      try {
        List<String> distinctIds = new ArrayList<>(new LinkedHashSet<>(allSmsToCancel));
        alimTalk.cancelAlimTalk(distinctIds);
      } catch (Exception ex) {
        log.error("Batch alimtalk-cancel failed. size={}", allSmsToCancel.size(), ex);
      }
    }
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
    if (entity.getCouponUuid() != null && entity.getIsCouponAlreadyRefund() == false) {
      couponUsageService.rollbackCouponUsage(entity.getCouponUuid());
      entity.setCouponAlreadyRefund(true);
      this.ticketRepository.save(entity);
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
