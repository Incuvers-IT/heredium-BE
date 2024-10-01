package art.heredium.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.account.entity.NonUser;
import art.heredium.domain.account.model.dto.request.GetUserNonUserTicketRequest;
import art.heredium.domain.account.repository.NonUserRepository;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.model.TicketUserInfo;
import art.heredium.domain.ticket.model.dto.request.PostTicketNonUserCommonRequest;
import art.heredium.domain.ticket.model.dto.request.PostTicketNonUserRequest;
import art.heredium.domain.ticket.model.dto.request.PostTicketNonUserValidRequest;
import art.heredium.domain.ticket.model.dto.response.GetUserMemberTicketDetailResponse;
import art.heredium.domain.ticket.model.dto.response.GetUserMemberTicketResponse;
import art.heredium.domain.ticket.model.dto.response.PostUserTicketResponse;
import art.heredium.domain.ticket.repository.TicketRepository;
import art.heredium.niceId.model.dto.response.PostNiceIdEncryptResponse;
import art.heredium.niceId.service.NiceIdService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TicketNonUserService {

  private final TicketPayService ticketPayService;
  private final TicketService ticketService;
  private final NonUserRepository nonUserRepository;
  private final NiceIdService niceIdService;
  private final TicketRepository ticketRepository;

  public Object valid(PostTicketNonUserValidRequest dto) {
    TicketUserInfo ticketUserInfo = createTicketUserInfo(dto.getUserRequest());
    return ticketPayService.valid(dto.getTicketOrderInfo(), ticketUserInfo);
  }

  public PostUserTicketResponse insert(PostTicketNonUserRequest dto) {
    return ticketPayService.insert(dto.getPayRequest());
  }

  public PostUserTicketResponse insert(PostTicketNonUserValidRequest dto) {
    TicketUserInfo ticketUserInfo = createTicketUserInfo(dto.getUserRequest());
    return ticketPayService.insert(dto.getTicketOrderInfo(), ticketUserInfo);
  }

  public TicketUserInfo createTicketUserInfo(PostTicketNonUserCommonRequest dto) {
    // 비회원
    PostNiceIdEncryptResponse info = niceIdService.decrypt(dto.getEncodeData());
    NonUser nonUser =
        nonUserRepository.findByPhoneAndHanaBankUuidIsNull(info.getMobileNo()).orElse(null);
    if (nonUser == null) {
      nonUser = new NonUser(info, dto);
      nonUser = nonUserRepository.save(nonUser);
    } else {
      nonUser.update(info, dto);
    }
    return new TicketUserInfo(nonUser, dto.getPassword());
  }

  /** 비회원 티켓 목록 */
  public Page<GetUserMemberTicketResponse> list(
      GetUserNonUserTicketRequest dto, Pageable pageable) {
    return ticketRepository.findByNonUser(dto, pageable);
  }

  /** 비회원 티켓 상세 정보 */
  public GetUserMemberTicketDetailResponse detail(Long id, GetUserNonUserTicketRequest dto) {
    Ticket entity =
        ticketRepository
            .findByIdAndNonUser_NameAndNonUser_PhoneAndPassword(
                id, dto.getName(), dto.getPhone(), dto.getPassword())
            .orElseThrow(() -> new ApiException(ErrorCode.DATA_NOT_FOUND));

    return ticketService.getTicketDetail(entity);
  }

  /** 비회원 환불 */
  public boolean refund(Long id, GetUserNonUserTicketRequest dto) {

    Ticket entity =
        ticketRepository
            .findByIdAndNonUser_NameAndNonUser_PhoneAndPassword(
                id, dto.getName(), dto.getPhone(), dto.getPassword())
            .orElseThrow(() -> new ApiException(ErrorCode.DATA_NOT_FOUND));
    ticketService.refund(entity);
    return true;
  }
}
