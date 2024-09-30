package art.heredium.domain.ticket.helper.validators;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.ticket.helper.chain.AbstractRoundValidator;
import art.heredium.domain.ticket.model.TicketCreateInfo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class VClosing extends AbstractRoundValidator {
    private final TicketCreateInfo info;

    @Override
    protected void validate() {
        if (info.getIsClose()) {
            throw new ApiException(ErrorCode.BAD_VALID, "회차 종료 30경 전까지만 예매가능", 4);
        }
    }
}
