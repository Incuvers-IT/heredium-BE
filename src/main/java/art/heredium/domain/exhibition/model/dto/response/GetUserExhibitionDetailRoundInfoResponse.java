package art.heredium.domain.exhibition.model.dto.response;

import art.heredium.domain.exhibition.entity.ExhibitionRound;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class GetUserExhibitionDetailRoundInfoResponse {
    private Long ticketNumber;
    private List<Info> infos;

    @Getter
    @Setter
    private static class Info {
        private Long id;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private RoundType type;

        private Info(ExhibitionRound entity, RoundType type) {
            this.id = entity.getId();
            this.startDate = entity.getStartDate();
            this.endDate = entity.getEndDate();
            this.type = type;
        }
    }

    public GetUserExhibitionDetailRoundInfoResponse(Long ticketNumber, List<ExhibitionRound> rounds, Map<String, Long> ticketTotalNumber) {
        this.ticketNumber = ticketNumber;
        this.infos = rounds.stream()
                .map(round -> {
                    Long totalCount = ticketTotalNumber.get(round.getTicketId());
                    boolean isSoldOut = !(round.getLimitNumber() > 0 && (totalCount == null || round.getLimitNumber() > totalCount));
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