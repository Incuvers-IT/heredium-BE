package art.heredium.controller.admin;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import art.heredium.domain.membership.model.dto.request.MembershipCreateRequest;
import art.heredium.domain.membership.model.dto.request.MembershipUpdateCouponRequest;
import art.heredium.domain.membership.model.dto.response.ActiveMembershipDetailResponse;
import art.heredium.domain.membership.model.dto.response.MembershipOptionResponse;
import art.heredium.domain.membership.model.dto.response.MembershipResponse;
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
import art.heredium.service.MembershipPaymentService;
import art.heredium.service.MembershipService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/memberships")
@ManagerPermission
public class AdminMembershipController {

  private final MembershipService membershipService;
  private final MembershipPaymentService membershipPaymentService;
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

  @GetMapping("/users/active/{accountId}")
  public ResponseEntity<Page<ActiveMembershipDetailResponse>> searchActiveMembershipsDetail(
          @PathVariable Long accountId,
          @Valid GetAllActiveMembershipsRequest request, Pageable pageable) {

    request.setAccountId(accountId);

    return ResponseEntity.ok(
            this.membershipService.listActiveMembershipsWithFilterDetail(request, pageable));
  }

  @GetMapping("/users/active/excel")
  @SupervisorPermission  public ModelAndView searchActiveMemberships(
      @Valid GetAllActiveMembershipsRequest request, @RequestParam("fileName") String fileName) {
    Map<String, Object> data = this.excelService.activeMembershipDownload(request, fileName);
    return new ModelAndView("xlsxView", data);
  }

  /**
   * 신규 멤버십을 생성합니다.
   *
   * @param request 신규 멤버십 생성에 필요한 정보 (이름, 가격, 기간 등)
   * @return 생성된 멤버십의 상세 정보
   */
  @PostMapping
  public ResponseEntity<?> createMembership(
          @Valid @RequestBody MembershipCreateRequest request) {

    // REST 관례에 따라 201 CREATED와 함께 Location 헤더를 반환할 수도 있습니다.
    return ResponseEntity.ok(membershipService.createMembership(request));
  }

  /**
   * 멤버십을 수정합니다.
   *
   */
  @PutMapping("/{membership-id}")
  public ResponseEntity<?> updateMembership(
          @PathVariable("membership-id") long membershipId,
          @Valid @RequestBody MembershipUpdateCouponRequest request) {

    membershipService.updateMembership(membershipId, request);
    return ResponseEntity.ok().build();
  }
  
  @GetMapping("/options")
  public ResponseEntity<List<MembershipOptionResponse>> getOptions() {
    List<MembershipOptionResponse> options = membershipService.listMembershipOptions();
    return ResponseEntity.ok(options);
  }

  /**
   * 단일 멤버십 상세 + 쿠폰 리스트
   */
  @GetMapping("/{membership-id}")
  public ResponseEntity<MembershipResponse> getMembershipDetail(
          @PathVariable("membership-id") long membershipId) {
    MembershipResponse detail = membershipService.getMembershipDetail(membershipId);
    return ResponseEntity.ok(detail);
  }
}
