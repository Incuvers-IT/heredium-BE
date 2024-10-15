package art.heredium.controller.admin;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.domain.coupon.model.dto.response.CouponUsageResponse;
import art.heredium.service.CouponUsageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {

  private final CouponUsageService couponUsageService;

  @GetMapping("/usage/{coupon-uuid}")
  public ResponseEntity<CouponUsageResponse> getCouponByUuid(
      @PathVariable(value = "coupon-uuid") String couponUuid) {
    return ResponseEntity.ok(this.couponUsageService.getCouponUsageByUuid(couponUuid));
  }

  @PostMapping("/checkout/{coupon-uuid}")
  public ResponseEntity<Void> checkoutCouponUsage(
      @PathVariable(value = "coupon-uuid") String couponUuid) {
    couponUsageService.checkoutCouponUsage(couponUuid);
    return ResponseEntity.ok().build();
  }
}
