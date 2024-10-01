package art.heredium.domain.ticket.helper.validators;

import lombok.AllArgsConstructor;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.ticket.helper.chain.AbstractRoundValidator;
import art.heredium.domain.ticket.model.TicketCreateInfo;

@AllArgsConstructor
public class VRoundDate extends AbstractRoundValidator {
  private final TicketCreateInfo info;

  @Override
  protected void validate() {
    if (info.getRoundStartDate().isBefore(info.getStartDate())
        || info.getRoundEndDate().isAfter(info.getEndDate())) {
      throw new ApiException(ErrorCode.BAD_VALID, "회차가 전시 시작일과 종료일 사이에 존재하지 않음", 2);
    }
  }
}
