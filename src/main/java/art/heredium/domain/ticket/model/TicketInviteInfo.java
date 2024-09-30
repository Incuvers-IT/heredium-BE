package art.heredium.domain.ticket.model;

import art.heredium.domain.ticket.model.dto.request.PostAdminTicketInviteRequest;

public interface TicketInviteInfo {
    TicketInviteCreateInfo getTicketCreateInfo(PostAdminTicketInviteRequest dto);
}