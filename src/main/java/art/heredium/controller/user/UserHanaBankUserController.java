package art.heredium.controller.user;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.hanabank.HanaParamsRequest;
import art.heredium.service.TicketHanaBankService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/hana-bank")
public class UserHanaBankUserController {

  private final TicketHanaBankService ticketHanaBankService;

  @PostMapping("/info")
  public ResponseEntity list(@RequestBody @Valid HanaParamsRequest dto) {
    return ResponseEntity.ok(ticketHanaBankService.getTicketHanaBankUserInfo(dto));
  }

  @PostMapping("/tickets")
  public ResponseEntity list(@RequestBody @Valid HanaParamsRequest dto, Pageable pageable) {
    return ResponseEntity.ok(ticketHanaBankService.list(dto, pageable));
  }

  @PostMapping("/tickets/{id}")
  public ResponseEntity detail(@PathVariable Long id, @RequestBody @Valid HanaParamsRequest dto) {
    return ResponseEntity.ok(ticketHanaBankService.detail(id, dto));
  }

  @PutMapping("/tickets/{id}/refund")
  public ResponseEntity ticketRefund(
      @PathVariable Long id, @RequestBody @Valid HanaParamsRequest dto) {
    return ResponseEntity.ok(ticketHanaBankService.refund(id, dto));
  }
}
