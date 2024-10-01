package art.heredium.controller.admin;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.service.DashBoardService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
public class AdminDashBoardController {

  private final DashBoardService dashBoardService;

  @GetMapping
  @ManagerPermission
  public ResponseEntity dashboard() {
    return ResponseEntity.ok(dashBoardService.dashboard());
  }
}
