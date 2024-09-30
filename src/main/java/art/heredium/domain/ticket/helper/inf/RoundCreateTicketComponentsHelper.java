package art.heredium.domain.ticket.helper.inf;

import art.heredium.domain.ticket.repository.TicketRepository;

public interface RoundCreateTicketComponentsHelper {
    TicketRepository getTicketRepository();
    Long getAccountId();
}
