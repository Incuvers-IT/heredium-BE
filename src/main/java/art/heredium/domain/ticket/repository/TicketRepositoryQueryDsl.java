package art.heredium.domain.ticket.repository;

import art.heredium.domain.account.model.dto.request.GetUserNonUserTicketRequest;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.domain.ticket.model.dto.request.GetAdminTicketRequest;
import art.heredium.domain.ticket.model.dto.response.GetUserMemberTicketEnabledResponse;
import art.heredium.domain.ticket.model.dto.response.GetUserMemberTicketResponse;
import art.heredium.domain.ticket.type.TicketKindType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TicketRepositoryQueryDsl {
    Page<Ticket> search(GetAdminTicketRequest dto, Pageable pageable);

    List<Ticket> search(GetAdminTicketRequest dto);

    Page<GetUserMemberTicketResponse> findByMember(Long accountId, List<TicketKindType> kinds, Integer year, Pageable pageable);

    Page<GetUserMemberTicketResponse> findByNonUser(GetUserNonUserTicketRequest dto, Pageable pageable);

    Page<GetUserMemberTicketResponse> findByNonUser(Long nonUserId, Pageable pageable);

    List<GetUserMemberTicketEnabledResponse> ticketByAccountAndEnabled(Long id);
}