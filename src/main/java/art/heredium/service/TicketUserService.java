package art.heredium.service;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.model.TicketUserInfo;
import art.heredium.domain.ticket.model.dto.request.PostTicketUserRequest;
import art.heredium.domain.ticket.model.dto.request.PostTicketUserValidRequest;
import art.heredium.domain.ticket.model.dto.response.GetUserMemberTicketDetailResponse;
import art.heredium.domain.ticket.model.dto.response.GetUserMemberTicketEnabledResponse;
import art.heredium.domain.ticket.model.dto.response.GetUserMemberTicketResponse;
import art.heredium.domain.ticket.model.dto.response.PostUserTicketResponse;
import art.heredium.domain.ticket.repository.TicketRepository;
import art.heredium.domain.ticket.type.TicketKindType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TicketUserService {

    private final TicketService ticketService;
    private final TicketPayService ticketPayService;
    private final TicketRepository ticketRepository;

    public Object valid(PostTicketUserValidRequest dto) {
        TicketUserInfo ticketUserInfo = createTicketUserInfo();
        return ticketPayService.valid(dto.getTicketOrderInfo(), ticketUserInfo);
    }

    public PostUserTicketResponse insert(PostTicketUserRequest dto) {
        return ticketPayService.insert(dto.getPayRequest());
    }

    public PostUserTicketResponse insert(PostTicketUserValidRequest dto) {
        TicketUserInfo ticketUserInfo = createTicketUserInfo();
        return ticketPayService.insert(dto.getTicketOrderInfo(), ticketUserInfo);
    }

    public TicketUserInfo createTicketUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        TicketUserInfo ticketUserInfo;
        boolean anonymousUser = authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser");
        if (anonymousUser) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "잘못된 접근");
        }
        //회원
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        ticketUserInfo = new TicketUserInfo(userPrincipal.getAccount());
        return ticketUserInfo;
    }

    /**
     * 입장가능한 티켓 목록
     */
    public List<GetUserMemberTicketEnabledResponse> ticketByAccountAndEnabled() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ticketRepository.ticketByAccountAndEnabled(userPrincipal.getId());
    }

    /**
     * 회원 티켓 목록
     */
    public Page<GetUserMemberTicketResponse> ticketByAccount(List<TicketKindType> kinds, Integer year, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ticketRepository.findByMember(userPrincipal.getId(), kinds, year, pageable);
    }

    /**
     * 회원 티켓 구매 연도 목록
     */
    public List<Integer> ticketYear(List<TicketKindType> kinds) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return ticketRepository.findTicketYear(userPrincipal.getId(), kinds);
    }

    /**
     * 회원 티켓 상세 정보
     */
    public GetUserMemberTicketDetailResponse detailByAccount(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Ticket entity = ticketRepository.findById(id).orElse(null);
        if (entity == null || entity.getAccount().getId().longValue() != userPrincipal.getId()) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }
        return ticketService.getTicketDetail(entity);
    }

    /**
     * 회원 환불
     */
    public boolean refundByAccount(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Ticket entity = ticketRepository.findById(id).orElse(null);
        if (entity == null || entity.getAccount().getId().longValue() != userPrincipal.getId()) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }
        ticketService.refund(entity);
        return true;
    }
}