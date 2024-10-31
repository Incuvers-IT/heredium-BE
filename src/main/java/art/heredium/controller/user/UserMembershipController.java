package art.heredium.controller.user;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.domain.membership.model.dto.request.MembershipConfirmPaymentRequest;
import art.heredium.domain.membership.model.dto.request.RegisterMembershipRequest;
import art.heredium.domain.membership.model.dto.response.MembershipConfirmPaymentResponse;
import art.heredium.domain.membership.model.dto.response.MembershipRegistrationResponse;
import art.heredium.domain.membership.model.dto.response.RegisterMembershipResponse;
import art.heredium.service.MembershipPaymentService;
import art.heredium.service.MembershipRegistrationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/membership")
public class UserMembershipController {
  private final MembershipRegistrationService membershipRegistrationService;
  private final MembershipPaymentService membershipPaymentService;

  @GetMapping(value = "/info")
  public ResponseEntity<MembershipRegistrationResponse> getMembershipRegistrationInfo() {
    return ResponseEntity.ok(this.membershipRegistrationService.getMembershipRegistrationInfo());
  }

  @PostMapping(value = "/register")
  public ResponseEntity<RegisterMembershipResponse> registerMembership(
      @RequestBody RegisterMembershipRequest request) {
    return ResponseEntity.ok(
        this.membershipRegistrationService.registerMembership(request.getMembershipId()));
  }

  @PostMapping(value = "/confirm-payment")
  public ResponseEntity<MembershipConfirmPaymentResponse> confirmPayment(
      @RequestBody MembershipConfirmPaymentRequest request) {
    return ResponseEntity.ok(this.membershipPaymentService.confirmPayment(request));
  }
}
