package art.heredium.domain.coffee.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.coffee.entity.CoffeeRound;

@Getter
@Setter
public class GetUserCoffeeDetailRoundInfoResponse {
  private Long ticketNumber;
  private List<Info> infos;

  @Getter
  @Setter
  private static class Info {
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private RoundType type;

    private Info(CoffeeRound entity, RoundType type) {
      this.id = entity.getId();
      this.startDate = entity.getStartDate();
      this.endDate = entity.getEndDate();
      this.type = type;
    }
  }

  public GetUserCoffeeDetailRoundInfoResponse(
      Long ticketNumber, List<CoffeeRound> rounds, Map<String, Long> ticketTotalNumber) {
    this.ticketNumber = ticketNumber;
    this.infos =
        rounds.stream()
            .map(
                round -> {
                  Long totalCount = ticketTotalNumber.get(round.getTicketId());
                  boolean isSoldOut =
                      !(round.getLimitNumber() > 0
                          && (totalCount == null || round.getLimitNumber() > totalCount));
                  RoundType type;
                  if (round.isClose()) {
                    type = RoundType.CLOSE;
                  } else if (isSoldOut) {
                    type = RoundType.SOLD_OUT;
                  } else {
                    type = RoundType.ENABLED;
                  }
                  return new Info(round, type);
                })
            .collect(Collectors.toList());
  }

  @Getter
  public enum RoundType {
    ENABLED(0, "예매 가능"),
    CLOSE(1, "마감"),
    SOLD_OUT(2, "매진"),
    ;

    private int code;
    private String desc;

    RoundType(int code, String desc) {
      this.code = code;
      this.desc = desc;
    }
  }
}
