package art.heredium.service;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.account.entity.NonUser;
import art.heredium.domain.account.model.dto.request.GetAdminHanaBankRequest;
import art.heredium.domain.account.model.dto.response.GetAdminAccountTicketResponse;
import art.heredium.domain.account.model.dto.response.GetAdminHanaBankUserDetailResponse;
import art.heredium.domain.account.model.dto.response.GetAdminHanaBankUserResponse;
import art.heredium.domain.account.repository.NonUserRepository;
import art.heredium.domain.ticket.repository.TicketRepository;
import art.heredium.domain.ticket.type.TicketKindType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class HanaBankUserService {

    private final NonUserRepository nonUserRepository;
    private final TicketRepository ticketRepository;

    public Page<GetAdminHanaBankUserResponse> list(GetAdminHanaBankRequest dto, Pageable pageable) {
        return nonUserRepository.search(dto, pageable);
    }

    public GetAdminHanaBankUserDetailResponse detailByAdmin(Long id) {
        NonUser entity = nonUserRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }
        return new GetAdminHanaBankUserDetailResponse(entity);
    }

    public Page<GetAdminAccountTicketResponse> ticketByHanaBankUser(Long id, Boolean isCoffee, Pageable pageable) {
        NonUser entity = nonUserRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new ApiException(ErrorCode.DATA_NOT_FOUND);
        }

        List<TicketKindType> ticketKindTypes = Arrays.stream(TicketKindType.values())
                .filter(kind -> isCoffee == (kind == TicketKindType.COFFEE))
                .collect(Collectors.toList());
        return ticketRepository.findAllByNonUser_IdAndKindInOrderByCreatedDateDesc(id, ticketKindTypes, pageable)
                .map(GetAdminAccountTicketResponse::new);
    }
}