package art.heredium.controller.admin;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import art.heredium.core.annotation.CoffeePermission;
import art.heredium.core.annotation.SupervisorPermission;
import art.heredium.domain.ticket.model.dto.request.GetAdminTicketRequest;
import art.heredium.domain.ticket.model.dto.request.PostAdminTicketGroupRequest;
import art.heredium.domain.ticket.model.dto.request.PostAdminTicketInviteRequest;
import art.heredium.domain.ticket.model.dto.response.GetAdminTicketResponse;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.excel.service.ExcelService;
import art.heredium.service.TicketService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/tickets")
public class AdminTicketsController {

  private final TicketService ticketService;
  private final ExcelService excelService;

  @GetMapping
  @CoffeePermission
  public ResponseEntity<Page<GetAdminTicketResponse>> list(
      @Valid GetAdminTicketRequest dto, Pageable pageable) {
    return ResponseEntity.ok(ticketService.list(dto, pageable));
  }

  @GetMapping("/{id}")
  @CoffeePermission
  public ResponseEntity detail(@PathVariable Long id) {
    return ResponseEntity.ok(ticketService.detailByAdmin(id));
  }

  @PutMapping("/{id}")
  @SupervisorPermission
  public ResponseEntity update(
      @PathVariable Long id, @RequestParam("state") TicketStateType state) {
    return ResponseEntity.ok(ticketService.update(id, state));
  }

  @GetMapping("/excel")
  @SupervisorPermission
  public ModelAndView listExcel(
      @Valid GetAdminTicketRequest dto, @RequestParam("fileName") String fileName) {
    Map<String, Object> data = excelService.ticketDownload(dto, fileName);
    return new ModelAndView("xlsxView", data);
  }

  @PostMapping("/group")
  @SupervisorPermission
  public ResponseEntity insert(@RequestBody @Valid PostAdminTicketGroupRequest dto) {
    return ResponseEntity.ok(ticketService.insertGroup(dto));
  }

  @PostMapping("/invite")
  @SupervisorPermission
  public ResponseEntity insert(@RequestBody @Valid PostAdminTicketInviteRequest dto) {
    return ResponseEntity.ok(ticketService.insertInvite(dto));
  }

  @PutMapping("/{ids}/refund")
  @CoffeePermission
  public ResponseEntity refund(@PathVariable List<Long> ids) {
    return ResponseEntity.ok(ticketService.refundByAdmin(ids));
  }

  @GetMapping("/statistics/dashboard")
  @CoffeePermission
  public ResponseEntity statisticsDashboard(
      @RequestParam("id") Long id, @RequestParam("kind") TicketKindType kind) {
    return ResponseEntity.ok(ticketService.statisticsDashboard(id, kind));
  }

  @PostMapping("/{id}/coffee/complete")
  @CoffeePermission
  public ResponseEntity coffeeComplete(@PathVariable Long id) {
    return ResponseEntity.ok(ticketService.coffeeComplete(id));
  }
}
