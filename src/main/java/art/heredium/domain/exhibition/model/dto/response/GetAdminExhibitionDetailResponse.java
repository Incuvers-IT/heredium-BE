package art.heredium.domain.exhibition.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.DiscountType;
import art.heredium.domain.common.type.HallType;
import art.heredium.domain.common.type.ProjectStateType;
import art.heredium.domain.exhibition.entity.*;

@Getter
@Setter
public class GetAdminExhibitionDetailResponse {
  private Long id;
  private Storage thumbnail;
  private Storage detailImage;
  private String title;
  private String subtitle;
  private List<HallType> halls;
  private Boolean isEnabled;
  private ProjectStateType state;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private LocalDateTime bookingDate;
  private String hour;
  private String contents;
  private String note;
  private Boolean isUseButton;
  private String buttonTitle;
  private String buttonLink;
  private Boolean isNewTab;
  private List<Round> rounds;
  private List<Price> prices;
  private List<Writer> writers;
  private String createdName;
  private LocalDateTime createdDate;
  private String lastModifiedName;
  private LocalDateTime lastModifiedDate;

  @Getter
  @Setter
  private static class Round {
    private Long id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer limitNumber;
    private Long ticketTotalCount;
    private Long ticketUsedCount;

    private Round(ExhibitionRound entity, Long ticketTotalNumber, Long ticketUsedCount) {
      this.id = entity.getId();
      this.startDate = entity.getStartDate();
      this.endDate = entity.getEndDate();
      this.limitNumber = entity.getLimitNumber();
      this.ticketTotalCount = ticketTotalNumber != null ? ticketTotalNumber : 0;
      this.ticketUsedCount = ticketUsedCount != null ? ticketUsedCount : 0;
    }
  }

  @Getter
  @Setter
  private static class Price {
    private Long id;
    private Boolean isEnabled;
    private String type;
    private Long price;
    private List<PriceDiscount> discounts;

    private Price(ExhibitionPrice entity) {
      this.id = entity.getId();
      this.isEnabled = entity.getIsEnabled();
      this.type = entity.getType();
      this.price = entity.getPrice();
      this.discounts =
          entity.getDiscounts().stream().map(PriceDiscount::new).collect(Collectors.toList());
    }

    @Getter
    @Setter
    private static class PriceDiscount {
      private Long id;
      private Long price;
      private String note;
      private DiscountType type;
      private Boolean enabled;

      private PriceDiscount(ExhibitionPriceDiscount entity) {
        this.id = entity.getId();
        this.price = entity.getPrice();
        this.note = entity.getNote();
        this.type = entity.getType();
        this.enabled = entity.getEnabled();
      }
    }
  }

  @Getter
  @Setter
  private static class Writer {
    private Long id;
    private String name;
    private List<WriterInfo> infos;

    private Writer(ExhibitionWriter entity) {
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

      private WriterInfo(ExhibitionWriterInfo entity) {
        this.id = entity.getId();
        this.thumbnail = entity.getThumbnail();
        this.name = entity.getName();
        this.intro = entity.getIntro();
      }
    }
  }

  public GetAdminExhibitionDetailResponse(
      Exhibition entity, Map<String, Long> ticketTotalNumber, Map<String, Long> ticketUsedCount) {
    this.id = entity.getId();
    this.thumbnail = entity.getThumbnail();
    this.detailImage = entity.getDetailImage();
    this.title = entity.getTitle();
    this.subtitle = entity.getSubtitle();
    this.halls = entity.getHalls();
    this.isEnabled = entity.getIsEnabled();
    this.state = entity.getState();
    this.startDate = entity.getStartDate();
    this.endDate = entity.getEndDate();
    this.bookingDate = entity.getBookingDate();
    this.hour = entity.getHour();
    this.contents = entity.getContents();
    this.note = entity.getNote();
    this.isUseButton = entity.getIsUseButton();
    this.buttonTitle = entity.getButtonTitle();
    this.buttonLink = entity.getButtonLink();
    this.isNewTab = entity.getIsNewTab();
    this.rounds =
        entity.getRounds().stream()
            .map(
                round ->
                    new Round(
                        round,
                        ticketTotalNumber.get(round.getTicketId()),
                        ticketUsedCount.get(round.getTicketId())))
            .collect(Collectors.toList());
    this.prices = entity.getPrices().stream().map(Price::new).collect(Collectors.toList());
    this.writers = entity.getWriters().stream().map(Writer::new).collect(Collectors.toList());
    this.createdName = entity.getCreatedName();
    this.createdDate = entity.getCreatedDate();
    this.lastModifiedName = entity.getLastModifiedName();
    this.lastModifiedDate = entity.getLastModifiedDate();
  }
}
