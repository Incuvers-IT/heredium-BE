package art.heredium.domain.coffee.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.coffee.entity.Coffee;
import art.heredium.domain.coffee.entity.CoffeeRound;

@Getter
@Setter
public class GetAdminCoffeeDetailRoundResponse {
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private String note;
  private List<Round> rounds;

  @Getter
  @Setter
  private static class Round {
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer limitNumber;
    private Long ticketTotalCount;
    private Long ticketUsedCount;

    private Round(CoffeeRound entity, Long ticketTotalCount, Long ticketUsedCount) {
      this.id = entity.getId();
      this.startDate = entity.getStartDate();
      this.endDate = entity.getEndDate();
      this.limitNumber = entity.getLimitNumber();
      this.ticketTotalCount = ticketTotalCount != null ? ticketTotalCount : 0;
      this.ticketUsedCount = ticketUsedCount != null ? ticketUsedCount : 0;
    }
  }

  public GetAdminCoffeeDetailRoundResponse(
      Coffee entity, Map<String, Long> ticketTotalNumber, Map<String, Long> ticketUsedCount) {
    this.startDate = entity.getStartDate();
    this.endDate = entity.getEndDate();
    this.note = entity.getNote();
    this.rounds =
        entity.getRounds().stream()
            .map(
                round ->
                    new Round(
                        round,
                        ticketTotalNumber.get(round.getTicketId()),
                        ticketUsedCount.get(round.getTicketId())))
            .collect(Collectors.toList());
  }
}
