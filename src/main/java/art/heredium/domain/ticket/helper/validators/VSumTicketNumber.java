package art.heredium.domain.ticket.helper.validators;

import lombok.AllArgsConstructor;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.ticket.helper.chain.AbstractRoundValidator;
import art.heredium.domain.ticket.model.TicketCreateInfo;
import art.heredium.domain.ticket.model.TicketUserInfo;
import art.heredium.domain.ticket.repository.TicketRepository;

@AllArgsConstructor
public class VSumTicketNumber extends AbstractRoundValidator {

  private final TicketRepository ticketRepository;
  private final TicketUserInfo ticketUserInfo;
  private final TicketCreateInfo info;

  @Override
  protected void validate() {
    Long sumTicketNumber =
        ticketRepository.sumTicketNumber(
            ticketUserInfo.getAccountId(),
            ticketUserInfo.getNonUserId(),
            info.getRoundStartDate(),
            info.getKind(),
            info.getKindId());
    long sumBuyNumber = info.getPrices().stream().mapToInt(TicketCreateInfo.Price::getNumber).sum();
    if (sumTicketNumber + sumBuyNumber > 4) { // 인당 하루에 최대 4매까지 구매가능
      throw new ApiException(ErrorCode.BAD_VALID, sumTicketNumber, 1);
    }
  }
}
