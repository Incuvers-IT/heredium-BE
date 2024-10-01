package art.heredium.controller.user;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.domain.ticket.model.dto.request.PostTicketGroupMailRequest;
import art.heredium.service.TicketService;

/** 회원 예매 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/tickets")
public class UserTicketController {

  private final TicketService ticketService;

  /** 티켓 QR코드 공유하기 정보 */
  @GetMapping("/info/{uuid}")
  public ResponseEntity ticketQrInfo(@PathVariable String uuid) {
    return ResponseEntity.ok(ticketService.ticketQrInfo(uuid));
  }

  /** 단체 예매 */
  @PostMapping("/group")
  public ResponseEntity groupMail(@RequestBody @Valid PostTicketGroupMailRequest dto) {
    return ResponseEntity.ok(ticketService.groupMail(dto));
  }
}
