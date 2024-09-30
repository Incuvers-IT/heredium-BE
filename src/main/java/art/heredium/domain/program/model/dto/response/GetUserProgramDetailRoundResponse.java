package art.heredium.domain.program.model.dto.response;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.DiscountType;
import art.heredium.domain.program.entity.Program;
import art.heredium.domain.program.entity.ProgramPrice;
import art.heredium.domain.program.entity.ProgramPriceDiscount;
import art.heredium.domain.program.entity.ProgramRound;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public class GetUserProgramDetailRoundResponse {
    private Long id;
    private Storage thumbnail;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String hour;
    private Map<LocalDate, Round> dates;
    private List<Price> prices;
    private LocalDate bookingStartDate;
    private LocalDate bookingEndDate;
    private Boolean isUseButton;
    private String buttonTitle;
    private String buttonLink;
    private Boolean isNewTab;

    @Getter
    @Setter
    private static class Round {
        private LocalDate date;
        private RoundType type;

        private Round(LocalDate date, RoundType type) {
            this.date = date;
            this.type = type;
        }
    }


    @Getter
    @Setter
    private static class Price {
        private Long id;
        private String type;
        private Long price;
        private List<PriceDiscount> discounts;

        private Price(ProgramPrice entity) {
            this.id = entity.getId();
            this.type = entity.getType();
            this.price = entity.getPrice();
            this.discounts = entity.getDiscounts().stream()
                    .filter(ProgramPriceDiscount::getEnabled)
                    .map(PriceDiscount::new)
                    .collect(Collectors.toList());
        }

        @Getter
        @Setter
        private static class PriceDiscount {
            private Long price;
            private String note;
            private DiscountType type;

            private PriceDiscount(ProgramPriceDiscount entity) {
                this.price = entity.getPrice();
                this.note = entity.getNote();
                this.type = entity.getType();
            }
        }
    }


    public GetUserProgramDetailRoundResponse(Program entity, Map<LocalDateTime, Long> ticketTotalNumber, List<LocalDate> holidays) {
        LocalDate bookingStartDate = entity.getBookingStartDate().toLocalDate();
        LocalDate bookingEndDate = entity.getBookingEndDate().toLocalDate();

        this.id = entity.getId();
        this.thumbnail = entity.getThumbnail();
        this.title = entity.getTitle();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.hour = entity.getHour();
        this.bookingStartDate = bookingStartDate;
        this.bookingEndDate = bookingEndDate;
        this.isUseButton = entity.getIsUseButton();
        this.buttonTitle = entity.getButtonTitle();
        this.buttonLink = entity.getButtonLink();
        this.isNewTab = entity.getIsNewTab();

        long priceCount = entity.getPrices().stream().filter(ProgramPrice::getIsEnabled).count();

        this.dates = entity.getRounds().stream()
                .filter(round -> !round.getStartDate().toLocalDate().isBefore(bookingStartDate) && !round.getStartDate().toLocalDate().isAfter(bookingEndDate))
                .map(round -> round.getStartDate().toLocalDate())
                .collect(Collectors.toSet())
                .stream().collect(Collectors.toMap(
                        date -> date,
                        date -> {
                            List<ProgramRound> roundsFilterDate = entity.getRounds().stream()
                                    .filter(round -> round.getStartDate().toLocalDate().isEqual(date))
                                    .collect(Collectors.toList());
                            boolean isSoldOut = roundsFilterDate.stream()
                                    .noneMatch(x -> {
                                        Long totalCount = ticketTotalNumber.get(x.getStartDate());
                                        return x.getLimitNumber() > 0 && (totalCount == null || x.getLimitNumber() > totalCount);
                                    });

                            boolean isHoliday = holidays.stream()
                                    .anyMatch(date::isEqual);

                            RoundType type;
                            if (priceCount == 0 || isHoliday || roundsFilterDate.size() == 0) {
                                type = RoundType.DISABLED;
                            } else if (isSoldOut) {
                                type = RoundType.SOLD_OUT;
                            } else {
                                type = RoundType.ENABLED;
                            }

                            return new Round(date, type);
                        }));

        this.prices = entity.getPrices().stream()
                .filter(ProgramPrice::getIsEnabled).map(Price::new)
                .collect(Collectors.toList());
    }

    @Getter
    public enum RoundType {
        ENABLED(0, "예매 가능"),
        DISABLED(1, "예매 불가"),
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
