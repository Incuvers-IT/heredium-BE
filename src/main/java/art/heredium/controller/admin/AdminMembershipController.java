package art.heredium.controller.admin;

import java.util.Map;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import art.heredium.core.annotation.ManagerPermission;
import art.heredium.core.annotation.SupervisorPermission;
import art.heredium.domain.membership.model.dto.request.GetAllActiveMembershipsRequest;
import art.heredium.domain.membership.model.dto.request.MembershipUpdateRequest;
import art.heredium.domain.membership.model.dto.response.ActiveMembershipRegistrationsResponse;
import art.heredium.excel.service.ExcelService;
import art.heredium.service.MembershipService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/memberships")
@ManagerPermission
public class AdminMembershipController {

  private final MembershipService membershipService;
  private final ExcelService excelService;

  @PutMapping("/{membership-id}/update-is-enabled")
  public ResponseEntity updateIsEnabled(
      @PathVariable("membership-id") long membershipId,
      @RequestBody MembershipUpdateRequest request) {
    this.membershipService.updateIsEnabled(membershipId, request.getIsEnabled());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/users/active")
  public ResponseEntity<Page<ActiveMembershipRegistrationsResponse>> searchActiveMemberships(
      @Valid GetAllActiveMembershipsRequest request, Pageable pageable) {
    return ResponseEntity.ok(
        this.membershipService.listActiveMembershipsWithFilter(request, pageable));
  }

  @GetMapping("/users/active/excel")
  @SupervisorPermission
  public ModelAndView searchActiveMemberships(
      @Valid GetAllActiveMembershipsRequest request, @RequestParam("fileName") String fileName) {
    Map<String, Object> data = this.excelService.activeMmbershipDownload(request, fileName);
    return new ModelAndView("xlsxView", data);
  }
}
