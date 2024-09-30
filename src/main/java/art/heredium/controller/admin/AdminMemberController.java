package art.heredium.controller.admin;

import art.heredium.domain.account.model.dto.request.PutAdminPasswordRequest;
import art.heredium.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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