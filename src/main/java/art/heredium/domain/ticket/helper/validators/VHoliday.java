package art.heredium.domain.ticket.helper.validators;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.holiday.component.HolidayManager;
import art.heredium.domain.ticket.helper.chain.AbstractRoundValidator;
import art.heredium.domain.ticket.model.TicketCreateInfo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class VHoliday extends AbstractRoundValidator {
    private final TicketCreateInfo info;

    @Override
    protected void validate() {
        if(HolidayManager.isHoliday(info.getRoundStartDate().toLocalDate()))
            throw new ApiException(ErrorCode.BAD_VALID, "휴관일", 0);
    }
}
