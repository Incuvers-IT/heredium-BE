package art.heredium.controller.admin;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.domain.account.model.dto.request.PutAdminPasswordRequest;
import art.heredium.service.AdminService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/member")
public class AdminMemberController {

  private final AdminService adminService;

  @PutMapping("/password")
  public ResponseEntity updatePassword(@RequestBody @Valid PutAdminPasswordRequest dto) {
    return ResponseEntity.ok(adminService.password(dto));
  }

  @GetMapping("/info")
  public ResponseEntity info() {
    return ResponseEntity.ok(adminService.info());
  }
}
