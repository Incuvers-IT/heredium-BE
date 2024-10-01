package art.heredium.domain.ticket.model;

import art.heredium.domain.ticket.model.dto.request.PostAdminTicketGroupRequest;

public interface TicketInfo {
  TicketCreateInfo getTicketCreateInfo(TicketOrderInfo dto);

  TicketCreateInfo getTicketCreateInfo(PostAdminTicketGroupRequest dto);

  String getTicketId();

  boolean isEnabledTicket();
}
