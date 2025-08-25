package art.heredium.controller.user;

import java.util.List;

import art.heredium.domain.coupon.model.dto.request.UserCouponUsageRequest;
import art.heredium.domain.coupon.model.dto.response.CouponUsagePage;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.jwt.JwtRedisUtil;
import art.heredium.core.util.AuthUtil;
import art.heredium.domain.coupon.model.dto.response.CouponResponseDto;
import art.heredium.domain.coupon.model.dto.response.CouponUsageResponse;
import art.heredium.service.CouponUsageService;

import static art.heredium.core.config.error.entity.ErrorCode.ANONYMOUS_USER;
import static art.heredium.service.TicketPayService.COUPON_USAGE_CACHE_KEY;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/coupons")
public class UserCouponController {

  private final CouponUsageService couponUsageService;
  private final JwtRedisUtil jwtRedisUtil;

  @GetMapping(value = "/usage", params = {"!page","!size"})
  public ResponseEntity<List<CouponResponseDto>> getCouponsWithUsageByAccountId() {
    final long accountId =
            AuthUtil.getCurrentUserAccountId().orElseThrow(() -> new ApiException(ANONYMOUS_USER));

    List<CouponResponseDto> couponResponseDtos =
            couponUsageService.getCouponsWithUsageByAccountId(accountId);
    return ResponseEntity.ok(couponResponseDtos);
  }
  // 페이지네이션을 적용을 위한 엔드포인트
  @GetMapping(value = "/usage", params = { "page", "size" })
  public ResponseEntity<CouponUsagePage> getCouponsWithUsageByAccountId(
          UserCouponUsageRequest req,
          Pageable pageable
  ) {
    final long accountId =
            AuthUtil.getCurrentUserAccountId().orElseThrow(() -> new ApiException(ANONYMOUS_USER));

    CouponUsagePage pageResp =
            couponUsageService.getCouponsWithUsagePage(accountId, req, pageable);

    return ResponseEntity.ok(pageResp);
  }

  @GetMapping("/usage/{coupon-uuid}")
  public ResponseEntity<CouponUsageResponse> getCouponByUuid(
      @PathVariable(value = "coupon-uuid") String couponUuid) {
    return ResponseEntity.ok(this.couponUsageService.getCouponUsageResponseByUuid(couponUuid));
  }

  @PostMapping("/uncheck-coupon/{coupon-uuid}")
  public ResponseEntity<Void> uncheckCoupon(@PathVariable("coupon-uuid") String couponUuid) {
    String couponCacheKey = COUPON_USAGE_CACHE_KEY + couponUuid;
    jwtRedisUtil.deleteData(couponCacheKey);
    return ResponseEntity.ok().build();
  }
}
