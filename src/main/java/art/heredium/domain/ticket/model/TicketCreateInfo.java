package art.heredium.domain.ticket.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.type.DiscountType;
import art.heredium.domain.ticket.type.TicketKindType;

@Getter
@Setter
public class TicketCreateInfo {
  private TicketKindType kind;
  private Long kindId;
  private Long roundId;
  private String title;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private LocalDateTime roundStartDate;
  private LocalDateTime roundEndDate;
  private Integer limitNumber;
  private LocalDateTime bookingEndDate;
  private Boolean isClose;
  private List<Price> prices;
  private DiscountType discountType;

  @Getter
  @Setter
  public static class Price {
    private String type;
    private Integer number;
    private Long price;
    private Long discountPrice;
    private String note;

    public Price(String type, Integer number, Long price, Long discountPrice, String note) {
      this.type = type;
      this.number = number;
      this.price = price;
      this.discountPrice = discountPrice;
      this.note = note;
    }
  }

  public TicketCreateInfo(
      TicketKindType kind,
      Long kindId,
      Long roundId,
      String title,
      LocalDateTime startDate,
      LocalDateTime endDate,
      LocalDateTime roundStartDate,
      LocalDateTime roundEndDate,
      Integer limitNumber,
      LocalDateTime bookingEndDate,
      Boolean isClose,
      List<Price> prices,
      DiscountType discountType) {
    this.kind = kind;
    this.kindId = kindId;
    this.roundId = roundId;
    this.title = title;
    this.startDate = startDate;
    this.endDate = endDate;
    this.roundStartDate = roundStartDate;
    this.roundEndDate = roundEndDate;
    this.limitNumber = limitNumber;
    this.bookingEndDate = bookingEndDate;
    this.isClose = isClose;
    this.prices = prices;
    this.discountType = discountType;
  }
}
