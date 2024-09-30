package art.heredium.controller.admin;

import art.heredium.domain.account.model.dto.request.PostAuthFindPwRequest;
import art.heredium.domain.account.model.dto.request.PostLoginRequest;
import art.heredium.service.AdminService;
import art.heredium.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AuthService authService;
    private final AdminService adminService;

    @PostMapping
    public ResponseEntity login(HttpServletResponse response, @RequestBody @Valid PostLoginRequest dto) {
        return ResponseEntity.ok(authService.login(response, dto, true));
    }

    @GetMapping("/refresh")
    public ResponseEntity refreshToken(HttpServletRequest request, HttpServletResponse response, @RequestParam("accessToken") String accessToken) {
        return ResponseEntity.ok(authService.refreshToken(request, response, accessToken));
    }

    @GetMapping("/find/id")
    public ResponseEntity findId(@RequestParam("encodeData") String encodeData) {
        return ResponseEntity.ok(adminService.findId(encodeData));
    }

    @GetMapping("/find/pw/phone")
    public ResponseEntity findPwByPhone(@RequestParam("email") String email, @RequestParam("encodeData") String encodeData) {
        return ResponseEntity.ok(adminService.findPwByPhone(email, encodeData));
    }

    @GetMapping("/find/pw/email")
    public ResponseEntity findPwByEmail(@RequestParam("email") String email, @RequestParam("redirectUrl") String redirectUrl) {
        return ResponseEntity.ok(adminService.findPwByEmail(email, redirectUrl));
    }

    @PutMapping("/find/pw")
    public ResponseEntity changePw(@RequestBody PostAuthFindPwRequest dto) {
        return ResponseEntity.ok(adminService.changePw(dto));
    }

    @GetMapping("/exist/email")
    public ResponseEntity existEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok(adminService.isExistEmail(email));
    }
}