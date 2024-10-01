package art.heredium.domain.ticket.model;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.type.DiscountType;
import art.heredium.domain.ticket.type.TicketKindType;

@Getter
@Setter
public class TicketOrderInfo {
  @NotNull private TicketKindType kind;
  @NotNull private Long roundId;

  @Size(min = 1)
  private List<@Valid Price> prices = new ArrayList<>();

  private DiscountType discountType;

  @Getter
  @Setter
  public static class Price {
    @NotNull private Long id;
    @NotNull private Integer number;
  }
}
