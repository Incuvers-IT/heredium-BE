package art.heredium.controller.user;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.domain.membership.model.dto.request.RegisterMembershipRequest;
import art.heredium.domain.membership.model.dto.response.MembershipRegistrationResponse;
import art.heredium.domain.membership.model.dto.response.RegisterMembershipResponse;
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

  @PostMapping(value = "/register")
  public ResponseEntity<RegisterMembershipResponse> registerMembership(
      @RequestBody RegisterMembershipRequest request) {
    return ResponseEntity.ok(
        new RegisterMembershipResponse(
            this.membershipRegistrationService.registerMembership(request.getMembershipId())));
  }
}
