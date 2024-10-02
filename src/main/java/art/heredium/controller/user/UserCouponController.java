package art.heredium.controller.user;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.coupon.model.dto.response.CouponResponseDto;
import art.heredium.service.CouponUsageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/coupons")
public class UserCouponController {

  private final CouponUsageService couponUsageService;

  @GetMapping("/usage")
  public ResponseEntity<List<CouponResponseDto>> getCouponsWithUsageByAccountId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    Long accountId = ((UserPrincipal) authentication.getPrincipal()).getId();

    List<CouponResponseDto> couponResponseDtos =
        couponUsageService.getCouponsWithUsageByAccountId(accountId);
    return ResponseEntity.ok(couponResponseDtos);
  }
}
