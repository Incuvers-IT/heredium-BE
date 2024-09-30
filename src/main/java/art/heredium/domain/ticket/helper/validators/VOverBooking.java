package art.heredium.domain.ticket.helper.validators;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.ticket.helper.chain.AbstractRoundValidator;
import art.heredium.domain.ticket.model.TicketCreateInfo;
import art.heredium.domain.ticket.repository.TicketRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class VOverBooking extends AbstractRoundValidator {

    private final TicketRepository ticketRepository;
    private final TicketCreateInfo info;

    @Override
    protected void validate() {
        Long bookingNumber = ticketRepository.sumBookingNumber(info.getKind(), info.getKindId(), info.getRoundId());
        if (bookingNumber >= info.getLimitNumber()) {
            throw new ApiException(ErrorCode.BAD_VALID, "매진된 회차", 5);
        }
    }
}
