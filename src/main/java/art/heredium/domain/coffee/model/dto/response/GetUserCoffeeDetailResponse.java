package art.heredium.domain.coffee.model.dto.response;

import art.heredium.domain.coffee.entity.*;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.DiscountType;
import art.heredium.domain.common.type.ProjectStateType;
import art.heredium.domain.exhibition.entity.ExhibitionPriceDiscount;
import art.heredium.domain.exhibition.model.dto.response.GetUserExhibitionDetailResponse;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class GetUserCoffeeDetailResponse {
    private Long id;
    private Storage thumbnail;
    private Storage detailImage;
    private String title;
    private String subtitle;
    private ProjectStateType state;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime bookingDate;
    private String hour;
    private String contents;
    private List<Price> prices;
    private List<Writer> writers;

    @Getter
    @Setter
    private static class Price {
        private Long id;
        private String type;
        private Long price;
        private List<PriceDiscount> discounts;

        private Price(CoffeePrice entity) {
            this.id = entity.getId();
            this.type = entity.getType();
            this.price = entity.getPrice();
            this.discounts = entity.getDiscounts().stream()
                    .filter(CoffeePriceDiscount::getEnabled)
                    .map(PriceDiscount::new)
                    .collect(Collectors.toList());
        }

        @Getter
        @Setter
        private static class PriceDiscount {
            private Long price;
            private String note;
            private DiscountType type;

            private PriceDiscount(CoffeePriceDiscount entity) {
                this.price = entity.getPrice();
                this.note = entity.getNote();
                this.type = entity.getType();
            }
        }
    }

    @Getter
    @Setter
    private static class Writer {
        private Long id;
        private String name;
        private List<WriterInfo> infos;

        private Writer(CoffeeWriter entity) {
            this.id = entity.getId();
            this.name = entity.getName();
            this.infos = entity.getInfos().stream().map(WriterInfo::new).collect(Collectors.toList());
        }

        @Getter
        @Setter
        private static class WriterInfo {
            private Long id;
            private Storage thumbnail;
            private String name;
            private String intro;

            private WriterInfo(CoffeeWriterInfo entity) {
                this.id = entity.getId();
                this.thumbnail = entity.getThumbnail();
                this.name = entity.getName();
                this.intro = entity.getIntro();
            }
        }
    }


    public GetUserCoffeeDetailResponse(Coffee entity) {
        this.id = entity.getId();
        this.thumbnail = entity.getThumbnail();
        this.detailImage = entity.getDetailImage();
        this.title = entity.getTitle();
        this.subtitle = entity.getSubtitle();
        this.state = entity.getState();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.bookingDate = entity.getBookingDate();
        this.hour = entity.getHour();
        this.contents = entity.getContents();
        this.prices = entity.getPrices().stream().filter(CoffeePrice::getIsEnabled).map(Price::new).collect(Collectors.toList());
        this.writers = entity.getWriters().stream().map(Writer::new).collect(Collectors.toList());
    }
}
