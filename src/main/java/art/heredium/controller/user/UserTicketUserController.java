package art.heredium.controller.user;

import javax.validation.Valid;

import art.heredium.payment.dto.PaymentsValidResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.domain.ticket.model.dto.request.PostTicketUserRequest;
import art.heredium.domain.ticket.model.dto.request.PostTicketUserValidRequest;
import art.heredium.service.TicketUserService;

/** 회원 예매 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/tickets/user")
public class UserTicketUserController {

  private final TicketUserService ticketUserService;

  /**
   * 유효성(가격, 쿠폰) 확인
   */
  @PostMapping("/valid")
  public ResponseEntity<PaymentsValidResponse> valid(
          @RequestBody @Valid PostTicketUserValidRequest dto
  ) {
    // dto 에는 ticketOrderInfo, couponUuid, membershipCouponId 가 들어있습니다.
    PaymentsValidResponse resp = ticketUserService.valid(dto);
    return ResponseEntity.ok(resp);
  }

//  @PostMapping("/valid")
//  public ResponseEntity valid(@RequestBody @Valid PostTicketUserValidRequest ticketOrderInfo) {
//    return ResponseEntity.ok(ticketUserService.valid(ticketOrderInfo));
//  }

  @PostMapping
  public ResponseEntity insert(@RequestBody @Valid PostTicketUserRequest dto) {
    return ResponseEntity.ok(ticketUserService.insert(dto));
  }

  @PostMapping("/free")
  public ResponseEntity insert(@RequestBody @Valid PostTicketUserValidRequest ticketOrderInfo) {
    return ResponseEntity.ok(ticketUserService.insert(ticketOrderInfo));
  }
}
