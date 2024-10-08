package art.heredium.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import org.apache.tika.utils.StringUtils;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.config.properties.HerediumProperties;
import art.heredium.core.jwt.JwtRedisUtil;
import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.entity.NonUser;
import art.heredium.domain.account.repository.AccountRepository;
import art.heredium.domain.account.repository.NonUserRepository;
import art.heredium.domain.common.type.DiscountType;
import art.heredium.domain.ticket.ProjectRounderRepository;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.entity.TicketPrice;
import art.heredium.domain.ticket.entity.TicketUuid;
import art.heredium.domain.ticket.helper.TicketRoundValidator;
import art.heredium.domain.ticket.helper.validators.*;
import art.heredium.domain.ticket.model.TicketCreateInfo;
import art.heredium.domain.ticket.model.TicketOrderInfo;
import art.heredium.domain.ticket.model.TicketUserInfo;
import art.heredium.domain.ticket.model.dto.response.PostUserTicketResponse;
import art.heredium.domain.ticket.repository.TicketRepository;
import art.heredium.domain.ticket.repository.TicketUuidRepository;
import art.heredium.ncloud.bean.CloudMail;
import art.heredium.ncloud.bean.HerediumAlimTalk;
import art.heredium.ncloud.type.AlimTalkTemplate;
import art.heredium.ncloud.type.MailTemplate;
import art.heredium.payment.inf.PaymentTicketResponse;
import art.heredium.payment.tosspayments.TossPayments;
import art.heredium.payment.tosspayments.dto.request.TossPaymentsPayRequest;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TicketPayService {

  private final HerediumProperties herediumProperties;
  private final RestTemplate restTemplate = new RestTemplate();
  private final HerediumAlimTalk alimTalk;
  private final TossPayments payment;

  private final AccountRepository accountRepository;
  private final NonUserRepository nonUserRepository;
  private final TicketRepository ticketRepository;
  private final TicketUuidRepository ticketUuidRepository;
  private final CloudMail cloudMail;
  private final JwtRedisUtil jwtRedisUtil;

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

  public Object valid(TicketOrderInfo ticketOrderInfo, TicketUserInfo ticketUserInfo) {
    // 결제 모듈 시작전 데이터 저장.
    Ticket entity = createTicket(ticketOrderInfo, ticketUserInfo, Constants.getUUID());
    jwtRedisUtil.setDataExpire(entity.getUuid(), ticketOrderInfo, 15 * 60);
    jwtRedisUtil.setDataExpire("ticketUserInfo-" + entity.getUuid(), ticketUserInfo, 15 * 60);
    return payment.valid(entity);
  }

  @Transactional(noRollbackFor = ApiException.class)
  public PostUserTicketResponse insert(TossPaymentsPayRequest dto) {
    // 결제모듈에서 결제완료후 시작전 저장한 데이터와 매칭
    TicketOrderInfo info = jwtRedisUtil.getData(dto.getOrderId(), TicketOrderInfo.class);
    TicketUserInfo ticketUserInfo =
        jwtRedisUtil.getData("ticketUserInfo-" + dto.getOrderId(), TicketUserInfo.class);
    if (info == null || ticketUserInfo == null) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }

    try {
      TicketUuid ticketUuid = ticketUuidRepository.findById(dto.getOrderId()).orElse(null);
      if (ticketUuid == null) {
        ticketUuidRepository.saveAndFlush(new TicketUuid(dto.getOrderId(), ticketUserInfo, info));
      }
    } catch (Exception e) {
      throw new ApiException(ErrorCode.DB_ERROR, e.getMessage());
    }

    Ticket entity = createTicket(info, ticketUserInfo, dto.getOrderId());
    PaymentTicketResponse pay = payment.pay(dto, entity.getPrice());
    entity.initPay(pay, payment.getPaymentType(dto));

    try {
      ticketRepository.saveAndFlush(entity);
      ticketUuidRepository.deleteById(entity.getUuid());

      Map<String, String> mailParam = entity.getMailParam(herediumProperties);
      if (!StringUtils.isBlank(entity.getEmail())) {
        cloudMail.mail(entity.getEmail(), mailParam, MailTemplate.TICKET_ISSUANCE);
      }
      alimTalk.sendAlimTalk(
          entity.getPhone(),
          entity.getMailParam(herediumProperties),
          AlimTalkTemplate.TICKET_ISSUANCE);
      List<String> smsRequestId =
          alimTalk.sendAlimTalk(
              entity.getPhone(),
              entity.getMailParam(herediumProperties),
              AlimTalkTemplate.TICKET_INFORMATION,
              entity.getStartDate().minusDays(1).withHour(10));
      entity.updateSmsRequestId(smsRequestId);
      ticketRepository.saveAndFlush(entity);
    } catch (Exception e) {
      log.error("티켓구매 에러", e);
      payment.cancel(entity, dto);
      throw new ApiException(ErrorCode.DB_ERROR, e.getMessage());
    }
    return new PostUserTicketResponse(entity);
  }

  public PostUserTicketResponse insert(TicketOrderInfo dto, TicketUserInfo ticketUserInfo) {

    Ticket entity = createTicket(dto, ticketUserInfo, Constants.getUUID());
    if (!StringUtils.isEmpty(entity.getPgId()) || entity.getPrice() > 0) {
      throw new ApiException(ErrorCode.BAD_VALID, "결제가 필요합니다.");
    }
    ticketRepository.saveAndFlush(entity);

    Map<String, String> mailParam = entity.getMailParam(herediumProperties);
    if (!StringUtils.isBlank(entity.getEmail())) {
      cloudMail.mail(entity.getEmail(), mailParam, MailTemplate.TICKET_ISSUANCE);
    }
    alimTalk.sendAlimTalk(
        entity.getPhone(),
        entity.getMailParam(herediumProperties),
        AlimTalkTemplate.TICKET_ISSUANCE);
    List<String> smsRequestId =
        alimTalk.sendAlimTalk(
            entity.getPhone(),
            entity.getMailParam(herediumProperties),
            AlimTalkTemplate.TICKET_INFORMATION,
            entity.getStartDate().minusDays(1).withHour(10));
    entity.updateSmsRequestId(smsRequestId);
    ticketRepository.saveAndFlush(entity);
    return new PostUserTicketResponse(entity);
  }

  private Ticket createTicket(TicketOrderInfo dto, TicketUserInfo ticketUserInfo, String uuid) {

    ProjectRounderRepository rounder = ProjectRounderRepository.finder(dto.getKind());
    TicketCreateInfo info = rounder.toTicketCreateInfo(dto);

    TicketRoundValidator ticketRoundValidator = new TicketRoundValidator();
    ticketRoundValidator
        .chain(new VHoliday(info))
        .chain(new VSumTicketNumber(ticketRepository, ticketUserInfo, info))
        .chain(new VRoundDate(info))
        .chain(new VBookingDate(info))
        .chain(new VClosing(info))
        .chain(new VOverBooking(ticketRepository, info));
    ticketRoundValidator.validate();

    boolean isDiscount;
    if (info.getDiscountType() == DiscountType.HANA_BANK) {
      if (!ticketUserInfo.getIsHanaBank()) {
        throw new ApiException(ErrorCode.BAD_REQUEST, "잘못 된 하나은행 할인 적용");
      }
      // 하나은행에서 구매한 티켓일때 할인율 적용
      isDiscount = true;
    } else {
      isDiscount = false;
    }

    List<TicketPrice> ticketPrices =
        info.getPrices().stream()
            .map(
                price ->
                    new TicketPrice(
                        null,
                        price.getType(),
                        price.getNumber(),
                        (isDiscount) ? price.getDiscountPrice() : price.getPrice(),
                        price.getPrice(),
                        isDiscount ? price.getNote() : null))
            .collect(Collectors.toList());

    Account account =
        ticketUserInfo.getAccountId() != null
            ? accountRepository.findById(ticketUserInfo.getAccountId()).orElse(null)
            : null;
    NonUser nonuser =
        ticketUserInfo.getNonUserId() != null
            ? nonUserRepository.findById(ticketUserInfo.getNonUserId()).orElse(null)
            : null;
    return new Ticket(
        ticketPrices,
        ticketUserInfo,
        info.getKind(),
        info.getKindId(),
        info.getRoundId(),
        account,
        nonuser,
        uuid,
        info.getTitle(),
        info.getRoundStartDate(),
        info.getRoundEndDate());
  }
}
