package art.heredium.domain.ticket.helper.validators;

import lombok.AllArgsConstructor;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.ticket.helper.chain.AbstractRoundValidator;
import art.heredium.domain.ticket.model.TicketCreateInfo;

@AllArgsConstructor
public class VBookingDate extends AbstractRoundValidator {
  private final TicketCreateInfo info;

  @Override
  protected void validate() {
    if (info.getRoundStartDate().isAfter(info.getBookingEndDate())) {
      throw new ApiException(ErrorCode.BAD_VALID, "예매 오픈일이 지나지 않았거나 회차 시작일이 2주 미만인 티켓이 아님", 3);
    }
  }
}
