package art.heredium.domain.program.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.DiscountType;
import art.heredium.domain.common.type.ProjectStateType;
import art.heredium.domain.program.entity.*;

@Getter
@Setter
public class GetUserProgramDetailResponse {
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

    private Price(ProgramPrice entity) {
      this.id = entity.getId();
      this.type = entity.getType();
      this.price = entity.getPrice();
      this.discounts =
          entity.getDiscounts().stream()
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

  @Getter
  @Setter
  private static class Writer {
    private Long id;
    private String name;
    private List<WriterInfo> infos;

    private Writer(ProgramWriter entity) {
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

      private WriterInfo(ProgramWriterInfo entity) {
        this.id = entity.getId();
        this.thumbnail = entity.getThumbnail();
        this.name = entity.getName();
        this.intro = entity.getIntro();
      }
    }
  }

  public GetUserProgramDetailResponse(Program entity) {
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
    this.prices =
        entity.getPrices().stream()
            .filter(ProgramPrice::getIsEnabled)
            .map(Price::new)
            .collect(Collectors.toList());
    this.writers = entity.getWriters().stream().map(Writer::new).collect(Collectors.toList());
  }
}
