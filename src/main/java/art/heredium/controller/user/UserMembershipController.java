package art.heredium.controller.user;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.domain.membership.model.dto.response.MembershipRegistrationResponse;
import art.heredium.service.MembershipRegistrationService;
import art.heredium.service.MembershipService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/membership")
public class UserMembershipController {
  private final MembershipRegistrationService membershipRegistrationService;
  private final MembershipService membershipService;

  @GetMapping(value = "/info")
  public ResponseEntity<MembershipRegistrationResponse> getMembershipRegistrationInfo() {
    return ResponseEntity.ok(
        new MembershipRegistrationResponse(
            this.membershipRegistrationService.getMembershipRegistrationInfo()));
  }
}
