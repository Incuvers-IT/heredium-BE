package art.heredium.domain.exhibition.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.exhibition.entity.Exhibition;
import art.heredium.domain.exhibition.entity.ExhibitionRound;

@Getter
@Setter
public class GetAdminExhibitionDetailRoundResponse {
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

    private Round(ExhibitionRound entity, Long ticketTotalCount, Long ticketUsedCount) {
      this.id = entity.getId();
      this.startDate = entity.getStartDate();
      this.endDate = entity.getEndDate();
      this.limitNumber = entity.getLimitNumber();
      this.ticketTotalCount = ticketTotalCount != null ? ticketTotalCount : 0;
      this.ticketUsedCount = ticketUsedCount != null ? ticketUsedCount : 0;
    }
  }

  public GetAdminExhibitionDetailRoundResponse(
      Exhibition entity, Map<String, Long> ticketTotalNumber, Map<String, Long> ticketUsedCount) {
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
