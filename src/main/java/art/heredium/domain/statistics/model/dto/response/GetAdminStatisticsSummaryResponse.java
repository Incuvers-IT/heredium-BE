package art.heredium.domain.statistics.model.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.querydsl.core.annotations.QueryProjection;

@Getter
@Setter
public class GetAdminStatisticsSummaryResponse {
  private Long visitNumber;
  private Long bookingNumber;
  private Long totalPrice;
  private List<TicketPriceInfo> ticketPriceInfo;

  @Getter
  @Setter
  public static class TicketPriceInfo {
    private String type;
    private Long unitPrice;
    private Integer saleNumber;
    private Integer refundNumber;
    private Long totalPrice;

    @QueryProjection
    public TicketPriceInfo(
        String type, Long unitPrice, Integer saleNumber, Integer refundNumber, Long totalPrice) {
      this.type = type;
      this.unitPrice = unitPrice;
      this.saleNumber = saleNumber;
      this.refundNumber = refundNumber;
      this.totalPrice = totalPrice;
    }
  }

  public GetAdminStatisticsSummaryResponse(
      Long visitNumber,
      Long bookingNumber,
      Long totalPrice,
      List<TicketPriceInfo> ticketPriceInfo) {
    this.visitNumber = visitNumber;
    this.bookingNumber = bookingNumber;
    this.totalPrice = totalPrice;
    this.ticketPriceInfo = ticketPriceInfo;
  }
}
