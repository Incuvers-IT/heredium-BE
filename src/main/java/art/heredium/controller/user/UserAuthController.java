package art.heredium.controller.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.domain.account.model.dto.request.PostAccountRequest;
import art.heredium.domain.account.model.dto.request.PostAuthFindPwRequest;
import art.heredium.domain.account.model.dto.request.PostLoginRequest;
import art.heredium.service.AccountService;
import art.heredium.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/auth")
public class UserAuthController {

  private final AccountService accountService;
  private final AuthService authService;

  @PostMapping
  public ResponseEntity login(
      HttpServletResponse response, @RequestBody @Valid PostLoginRequest dto) {
    return ResponseEntity.ok(authService.login(response, dto, false));
  }

  @GetMapping("/refresh")
  public ResponseEntity refreshToken(
      HttpServletRequest request,
      HttpServletResponse response,
      @RequestParam("accessToken") String accessToken) {
    return ResponseEntity.ok(authService.refreshToken(request, response, accessToken));
  }

  @PostMapping("/release")
  public ResponseEntity sleeperRelease(@RequestParam("accessToken") String accessToken) {
    return ResponseEntity.ok(authService.sleeperRelease(accessToken));
  }

  @PostMapping("/sign-up")
  public ResponseEntity insert(
      HttpServletResponse response, @RequestBody @Valid PostAccountRequest dto) {
    return ResponseEntity.ok(accountService.insert(response, dto));
  }

  @GetMapping("/find/id")
  public ResponseEntity findId(@RequestParam("encodeData") String encodeData) {
    return ResponseEntity.ok(accountService.findId(encodeData));
  }

  @GetMapping("/find/pw/phone")
  public ResponseEntity findPwByPhone(
      @RequestParam("email") String email, @RequestParam("encodeData") String encodeData) {
    return ResponseEntity.ok(accountService.findPwByPhone(email, encodeData));
  }

  @GetMapping("/find/pw/email")
  public ResponseEntity findPwByEmail(
      @RequestParam("email") String email, @RequestParam("redirectUrl") String redirectUrl) {
    return ResponseEntity.ok(accountService.findPwByEmail(email, redirectUrl));
  }

  @PostMapping("/find/pw")
  public ResponseEntity changePw(@RequestBody PostAuthFindPwRequest dto) {
    return ResponseEntity.ok(accountService.changePw(dto));
  }

  @GetMapping("/exist/email")
  public ResponseEntity existEmail(@RequestParam("email") String email) {
    return ResponseEntity.ok(accountService.isExistEmail(email));
  }
}
