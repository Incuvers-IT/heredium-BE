package art.heredium.controller.admin;

import art.heredium.domain.coupon.model.dto.request.RecurringCouponCreateRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import art.heredium.domain.coupon.model.dto.request.CouponAssignRequest;
import art.heredium.domain.coupon.model.dto.request.NonMembershipCouponCreateRequest;
import art.heredium.domain.coupon.model.dto.response.CouponResponse;
import art.heredium.domain.coupon.model.dto.response.CouponUsageCheckResponse;
import art.heredium.domain.coupon.model.dto.response.CouponUsageResponse;
import art.heredium.service.CouponService;
import art.heredium.service.CouponUsageService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {

  private final CouponService couponService;
  private final CouponUsageService couponUsageService;

  @GetMapping("/usage/{coupon-uuid}")
  public ResponseEntity<CouponUsageResponse> getCouponByUuid(
      @PathVariable(value = "coupon-uuid") String couponUuid) {
    return ResponseEntity.ok(this.couponUsageService.getCouponUsageResponseByUuid(couponUuid));
  }

  @PostMapping("/checkout/{coupon-uuid}")
  public ResponseEntity<Void> checkoutCouponUsage(
      @PathVariable(value = "coupon-uuid") String couponUuid) {
    couponUsageService.checkoutCouponUsage(couponUuid);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/non-membership")
  public ResponseEntity<CouponResponse> createNonMembershipCoupon(
      @RequestBody final RecurringCouponCreateRequest request) {
    return ResponseEntity.ok(this.couponService.createNonMembershipCoupon(request));
  }

  @GetMapping("/{coupon-id}")
  public ResponseEntity<CouponResponse> getCouponDetail(
      @PathVariable("coupon-id") final long couponId) {
    return ResponseEntity.ok(this.couponService.getCouponDetail(couponId));
  }

  @PostMapping("/assign")
  public ResponseEntity<Void> assignCouponToAccounts(
      @RequestBody final CouponAssignRequest request) {
    this.couponUsageService.assignCoupons(request.getCouponId(), request.getAccountIds());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/check-usage/membership/{membershipRegistrationId}")
  public ResponseEntity<CouponUsageCheckResponse> checkMembershipCouponUsage(
      @PathVariable("membershipRegistrationId") final long membershipRegistrationId) {
    return ResponseEntity.ok(
        this.couponUsageService.checkActiveMembershipCouponUsage(membershipRegistrationId));
  }

  @GetMapping("/all")
  public ResponseEntity<List<CouponResponse>> getAllCoupons() {
    return ResponseEntity.ok(this.couponService.getAllCoupons());
  }

  @DeleteMapping("/{couponId}")
  public ResponseEntity<Void> deleteCoupon(@PathVariable Long couponId) {
    couponService.deleteCoupon(couponId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{coupon-id}")
  public ResponseEntity<CouponResponse> updateCoupon(
          @PathVariable("coupon-id") long couponId,
          @RequestBody RecurringCouponCreateRequest request) {
    return ResponseEntity.ok(this.couponService.updateCoupon(couponId, request));
  }

}
