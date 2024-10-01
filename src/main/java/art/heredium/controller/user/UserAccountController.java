package art.heredium.controller.user;

import java.util.List;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.domain.account.model.dto.request.PutUserAccountRequest;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.service.AccountService;
import art.heredium.service.TicketUserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/account")
public class UserAccountController {

  private final AccountService accountService;
  private final TicketUserService ticketUserService;

  @GetMapping
  public ResponseEntity get(@RequestParam String password) {
    return ResponseEntity.ok(accountService.get(password));
  }

  @GetMapping("/info")
  public ResponseEntity info() {
    return ResponseEntity.ok(accountService.info());
  }

  @GetMapping("/tickets")
  public ResponseEntity tickets(
      @RequestParam("kinds") List<TicketKindType> kinds,
      @RequestParam(value = "year", required = false) Integer year,
      Pageable pageable) {
    return ResponseEntity.ok(ticketUserService.ticketByAccount(kinds, year, pageable));
  }

  @GetMapping("/tickets/year")
  public ResponseEntity ticketYear(@RequestParam("kinds") List<TicketKindType> kinds) {
    return ResponseEntity.ok(ticketUserService.ticketYear(kinds));
  }

  @GetMapping("/tickets/enabled")
  public ResponseEntity tickets() {
    return ResponseEntity.ok(ticketUserService.ticketByAccountAndEnabled());
  }

  @GetMapping("/tickets/{id}")
  public ResponseEntity ticketDetail(@PathVariable Long id) {
    return ResponseEntity.ok(ticketUserService.detailByAccount(id));
  }

  @PutMapping("/tickets/{id}/refund")
  public ResponseEntity ticketRefund(@PathVariable Long id) {
    return ResponseEntity.ok(ticketUserService.refundByAccount(id));
  }

  @PutMapping
  public ResponseEntity updateByAccount(@RequestBody @Valid PutUserAccountRequest dto) {
    return ResponseEntity.ok(accountService.updateByAccount(dto));
  }

  @PutMapping("/local-resident")
  public ResponseEntity updateLocalResident(@RequestParam("enabled") Boolean isEnabled) {
    return ResponseEntity.ok(accountService.updateLocalResident(isEnabled));
  }

  @DeleteMapping
  public ResponseEntity delete(@RequestParam("password") String password) {
    return ResponseEntity.ok(accountService.delete(password));
  }
}
