package art.heredium.controller.admin;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.domain.membership.model.dto.request.GetAllActiveMembershipsRequest;
import art.heredium.domain.membership.model.dto.request.MembershipUpdateRequest;
import art.heredium.domain.membership.model.dto.response.ActiveMembershipRegistrationsResponse;
import art.heredium.service.MembershipService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/memberships")
@ManagerPermission
public class AdminMembershipController {

  private final MembershipService membershipService;

  @PutMapping("/{membership-id}/update-is-enabled")
  public ResponseEntity updateIsEnabled(
      @PathVariable("membership-id") long membershipId,
      @RequestBody MembershipUpdateRequest request) {
    this.membershipService.updateIsEnabled(membershipId, request.getIsEnabled());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/active")
  public ResponseEntity<Page<ActiveMembershipRegistrationsResponse>> searchActiveMemberships(
      @Valid GetAllActiveMembershipsRequest request, Pageable pageable) {
    return ResponseEntity.ok(
        this.membershipService.listActiveMembershipsWithFilter(request, pageable));
  }
}
