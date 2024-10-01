package art.heredium.controller.admin;

import java.util.Map;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import art.heredium.core.annotation.SupervisorPermission;
import art.heredium.domain.account.model.dto.request.*;
import art.heredium.excel.service.ExcelService;
import art.heredium.service.AccountService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/accounts")
public class AdminAccountController {

  private final AccountService accountService;
  private final ExcelService excelService;

  @GetMapping
  @SupervisorPermission
  public ResponseEntity list(@Valid GetAdminAccountRequest dto, Pageable pageable) {
    return ResponseEntity.ok(accountService.list(dto, pageable));
  }

  @GetMapping("/{id}")
  @SupervisorPermission
  public ResponseEntity detail(@PathVariable Long id) {
    return ResponseEntity.ok(accountService.detailByAdmin(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity update(
      @PathVariable Long id, @RequestBody @Valid PutAdminAccountRequest dto) {
    return ResponseEntity.ok(accountService.update(id, dto));
  }

  @GetMapping("/duplicate")
  @SupervisorPermission
  public ResponseEntity duplicate(@RequestParam("email") String email) {
    return ResponseEntity.ok(accountService.isExistEmail(email));
  }

  @GetMapping("/ticket/group")
  @SupervisorPermission
  public ResponseEntity list(@Valid GetAccountTicketGroupRequest dto, Pageable pageable) {
    return ResponseEntity.ok(accountService.ticketGroup(dto, pageable));
  }

  @GetMapping("/ticket/invite")
  @SupervisorPermission
  public ResponseEntity list(@Valid GetAccountTicketInviteRequest dto, Pageable pageable) {
    return ResponseEntity.ok(accountService.ticketInvite(dto, pageable));
  }

  @GetMapping("/excel")
  @SupervisorPermission
  public ModelAndView listExcel(
      @Valid GetAdminAccountRequest dto, @RequestParam("fileName") String fileName) {
    Map<String, Object> data = excelService.accountDownload(dto, fileName);
    return new ModelAndView("xlsxView", data);
  }

  @GetMapping("/{id}/tickets")
  @SupervisorPermission
  public ResponseEntity tickets(
      @PathVariable Long id, @RequestParam("isCoffee") Boolean isCoffee, Pageable pageable) {
    return ResponseEntity.ok(accountService.ticketByAccount(id, isCoffee, pageable));
  }

  @PutMapping("/{id}/password")
  public ResponseEntity password(
      @PathVariable Long id, @RequestBody @Valid PutAccountPasswordRequest dto) {
    return ResponseEntity.ok(accountService.password(id, dto));
  }

  @GetMapping("/sleepers")
  public ResponseEntity sleepers(@Valid GetAdminSleeperRequest dto, Pageable pageable) {
    return ResponseEntity.ok(accountService.sleepers(dto, pageable));
  }

  @GetMapping("/sleepers/excel")
  @SupervisorPermission
  public ModelAndView sleeperExcel(
      @Valid GetAdminSleeperRequest dto, @RequestParam("fileName") String fileName) {
    Map<String, Object> data = excelService.sleeperDownload(dto, fileName);
    return new ModelAndView("xlsxView", data);
  }
}
