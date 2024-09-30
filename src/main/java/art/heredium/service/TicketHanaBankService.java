package art.heredium.service;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.NonUser;
import art.heredium.domain.account.repository.NonUserRepository;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.model.TicketHanaBankUserInfo;
import art.heredium.domain.ticket.model.TicketUserInfo;
import art.heredium.domain.ticket.model.dto.request.PostTicketHanaBankRequest;
import art.heredium.domain.ticket.model.dto.request.PostTicketHanaBankUserCommonRequest;
import art.heredium.domain.ticket.model.dto.request.PostTicketHanaBankValidRequest;
import art.heredium.domain.ticket.model.dto.response.GetUserMemberTicketDetailResponse;
import art.heredium.domain.ticket.model.dto.response.GetUserMemberTicketResponse;
import art.heredium.domain.ticket.model.dto.response.PostUserTicketResponse;
import art.heredium.domain.ticket.repository.TicketRepository;
import art.heredium.hanabank.HanaParams;
import art.heredium.hanabank.HanaParamsRequest;
import art.heredium.hanabank.HanaParamsResponse;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class TicketHanaBankService {

    private final TicketPayService ticketPayService;
    private final TicketService ticketService;
    private final TicketRepository ticketRepository;
    private final NonUserRepository nonUserRepository;
    private final Validator validator;

    @Value("${app.hana-bank.partner_key}")
    private String PARTNER_KEY;

    @Value("${app.hana-bank.partner_salt}")
    private String PARTNER_SALT;

    public Object valid(PostTicketHanaBankValidRequest dto) {
        TicketUserInfo ticketUserInfo = getTicketUserInfo(dto.getUserRequest());
        return ticketPayService.valid(dto.getTicketOrderInfo(), ticketUserInfo);
    }

    public PostUserTicketResponse insert(PostTicketHanaBankRequest dto) {
        return ticketPayService.insert(dto.getPayRequest());
    }

    public PostUserTicketResponse insert(PostTicketHanaBankValidRequest dto) {
        TicketUserInfo ticketUserInfo = getTicketUserInfo(dto.getUserRequest());
        return ticketPayService.insert(dto.getTicketOrderInfo(), ticketUserInfo);
    }

    public TicketUserInfo getTicketUserInfo(PostTicketHanaBankUserCommonRequest dto) {
        TicketHanaBankUserInfo info;
        info = getTicketHanaBankUserInfo(dto);

        NonUser nonUser = nonUserRepository.findByHanaBankUuid(info.getHanaBankUuid()).orElse(null);
        if (nonUser == null) {
            nonUser = new NonUser(info, dto);
            nonUser = nonUserRepository.save(nonUser);
        } else {
            nonUser.update(info, dto);
        }

        return new TicketUserInfo(nonUser, dto.getPassword());
    }

    public TicketHanaBankUserInfo getTicketHanaBankUserInfo(HanaParamsRequest dto) {
        TicketHanaBankUserInfo info;
        try {
            DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
                    .appendPattern("yyyyMMddHHmmss")
                    .appendValue(ChronoField.MILLI_OF_SECOND, 3)
                    .toFormatter();
            LocalDateTime nonce = LocalDateTime.parse(dto.getNonce(), dateTimeFormatter);
            if (nonce.plusMinutes(30).isBefore(Constants.getNow())) {
                throw new ApiException(ErrorCode.TIMEOUT, "요청시간 만료");
            }
            String hanaData = HanaParams.parse(PARTNER_KEY, PARTNER_SALT, dto.getMessage(), dto.getMac(), dto.getNonce());
            HanaParamsResponse hanaParamsResponse = new Gson().fromJson(hanaData, HanaParamsResponse.class);
            info = new TicketHanaBankUserInfo(hanaParamsResponse);
            validator.validate(info);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.BAD_REQUEST);
        }
        return info;
    }

    /**
     * 하나은행 티켓 목록
     */
    public Page<GetUserMemberTicketResponse> list(HanaParamsRequest dto, Pageable pageable) {
        TicketHanaBankUserInfo ticketHanaBankUserInfo = getTicketHanaBankUserInfo(dto);
        NonUser nonUser = nonUserRepository.findByHanaBankUuid(ticketHanaBankUserInfo.getHanaBankUuid())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return ticketRepository.findByNonUser(nonUser.getId(), pageable);
    }

    /**
     * 하나은행 티켓 상세 정보
     */
    public GetUserMemberTicketDetailResponse detail(Long id, HanaParamsRequest dto) {
        TicketHanaBankUserInfo ticketHanaBankUserInfo = getTicketHanaBankUserInfo(dto);
        NonUser nonUser = nonUserRepository.findByHanaBankUuid(ticketHanaBankUserInfo.getHanaBankUuid())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Ticket entity = ticketRepository.findById(id).orElse(null);
        if (entity == null ||
            entity.getNonUser() == null || entity.getNonUser().getId().longValue() != nonUser.getId().longValue()) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }
        return ticketService.getTicketDetail(entity);
    }

    /**
     * 하나은행 환불
     */
    public boolean refund(Long id, HanaParamsRequest dto) {

        TicketHanaBankUserInfo ticketHanaBankUserInfo = getTicketHanaBankUserInfo(dto);
        NonUser nonUser = nonUserRepository.findByHanaBankUuid(ticketHanaBankUserInfo.getHanaBankUuid())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Ticket entity = ticketRepository.findById(id).orElse(null);
        if (entity == null ||
            entity.getNonUser() == null || entity.getNonUser().getId().longValue() != nonUser.getId().longValue()) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }
        ticketService.refund(entity);
        return true;
    }
}