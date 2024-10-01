package art.heredium.controller.admin;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.core.annotation.AdminPermission;
import art.heredium.domain.ticket.model.dto.request.PostTicketQrRequest;
import art.heredium.service.CommonService;
import art.heredium.service.TicketService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/app/v1")
public class AdminAppController {

  private final TicketService ticketService;
  private final CommonService commonService;

  @PostMapping("/qr")
  @AdminPermission
  public ResponseEntity usedQR(@RequestBody @Valid PostTicketQrRequest dto) {
    return ResponseEntity.ok(ticketService.qrUse(dto));
  }

  @GetMapping("/projects")
  @AdminPermission
  public ResponseEntity projects() {
    return ResponseEntity.ok(commonService.projects());
  }
}
