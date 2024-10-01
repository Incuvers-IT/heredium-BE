package art.heredium.controller.user;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.domain.ticket.model.dto.request.PostTicketHanaBankRequest;
import art.heredium.domain.ticket.model.dto.request.PostTicketHanaBankValidRequest;
import art.heredium.service.TicketHanaBankService;

/** 하나은행 예매 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/tickets/hana-bank")
public class UserTicketHanaBankController {

  private final TicketHanaBankService ticketHanaBankService;

  @PostMapping("/valid")
  public ResponseEntity hanaBankValid(@RequestBody @Valid PostTicketHanaBankValidRequest dto) {
    return ResponseEntity.ok(ticketHanaBankService.valid(dto));
  }

  @PostMapping
  public ResponseEntity hanaBankInsert(@RequestBody @Valid PostTicketHanaBankRequest dto) {
    return ResponseEntity.ok(ticketHanaBankService.insert(dto));
  }

  @PostMapping("/free")
  public ResponseEntity hanaBankInsert(@RequestBody @Valid PostTicketHanaBankValidRequest dto) {
    return ResponseEntity.ok(ticketHanaBankService.insert(dto));
  }
}
