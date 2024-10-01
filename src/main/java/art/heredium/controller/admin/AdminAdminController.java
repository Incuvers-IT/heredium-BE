package art.heredium.controller.admin;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.core.annotation.AdminPermission;
import art.heredium.domain.account.model.dto.request.PostAdminRequest;
import art.heredium.domain.account.model.dto.request.PutAccountPasswordRequest;
import art.heredium.domain.account.model.dto.request.PutAdminRequest;
import art.heredium.service.AdminService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/admins")
public class AdminAdminController {

  private final AdminService adminService;

  @GetMapping
  @AdminPermission
  public ResponseEntity list(Pageable pageable) {
    return ResponseEntity.ok(adminService.list(pageable));
  }

  @GetMapping("/{id}")
  @AdminPermission
  public ResponseEntity detail(@PathVariable Long id) {
    return ResponseEntity.ok(adminService.detail(id));
  }

  @PostMapping
  @AdminPermission
  public ResponseEntity insert(@RequestBody @Valid PostAdminRequest dto) {
    return ResponseEntity.ok(adminService.insert(dto, false));
  }

  @PutMapping("/{id}")
  @AdminPermission
  public ResponseEntity update(@PathVariable Long id, @RequestBody @Valid PutAdminRequest dto) {
    return ResponseEntity.ok(adminService.update(id, dto));
  }

  @DeleteMapping("/{id}")
  @AdminPermission
  public ResponseEntity delete(@PathVariable Long id) {
    return ResponseEntity.ok(adminService.delete(id));
  }

  @PutMapping("/{id}/password")
  public ResponseEntity password(
      @PathVariable Long id, @RequestBody @Valid PutAccountPasswordRequest dto) {
    return ResponseEntity.ok(adminService.password(id, dto));
  }

  @GetMapping("/duplicate")
  @AdminPermission
  public ResponseEntity duplicate(@RequestParam("email") String email) {
    return ResponseEntity.ok(adminService.isExistEmail(email));
  }
}
