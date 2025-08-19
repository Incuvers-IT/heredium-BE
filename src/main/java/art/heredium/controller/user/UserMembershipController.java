package art.heredium.controller.user;

import art.heredium.domain.membership.model.dto.request.RegisterMembershipRequest;
import art.heredium.domain.membership.model.dto.response.MembershipBenefitResponse;
import art.heredium.domain.membership.model.dto.response.MembershipRegistrationResponse;
import art.heredium.domain.membership.model.dto.response.MembershipResponse;
import art.heredium.domain.membership.model.dto.response.RegisterMembershipResponse;
import art.heredium.service.MembershipPaymentService;
import art.heredium.service.MembershipRegistrationService;
import art.heredium.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/membership")
public class UserMembershipController {
  private final MembershipRegistrationService membershipRegistrationService;
  private final MembershipPaymentService membershipPaymentService;
  private final MembershipService membershipService;

  @GetMapping("/info")
  public ResponseEntity<MembershipRegistrationResponse> getMembershipRegistrationInfo() {
    return ResponseEntity.ok(membershipRegistrationService.getMembershipRegistrationInfo());
  }

  @GetMapping("/benefit")
  public ResponseEntity<MembershipBenefitResponse> getMembershipBenefitList() {
    return ResponseEntity.ok(membershipService.getMembershipBenefitList());
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterMembershipResponse> registerMembership(
          @RequestBody RegisterMembershipRequest request) {
    return ResponseEntity.ok(
            membershipRegistrationService.registerMembership(request.getMembershipId()));
  }

  @GetMapping("/code/{code}")
  public ResponseEntity<MembershipResponse> getByCode(@PathVariable int code) {
    return ResponseEntity.ok(membershipRegistrationService.getMembershipByCode(code));
  }
}
