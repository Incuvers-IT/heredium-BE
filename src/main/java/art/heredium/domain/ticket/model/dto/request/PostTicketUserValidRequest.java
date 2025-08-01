package art.heredium.domain.ticket.model.dto.request;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.ticket.model.TicketOrderInfo;

@Getter
@Setter
public class PostTicketUserValidRequest {

  @NotNull private @Valid TicketOrderInfo ticketOrderInfo;
  /**
   * 일반 쿠폰(사용된 coupon_usage.uuid)을 보낼 때 사용
   */
  private String couponUuid;

  /**
   * 멤버십 쿠폰을 보낼 때는 membership 테이블의 coupon.id 를 보내세요
   */
  private Long membershipCouponId;
}
