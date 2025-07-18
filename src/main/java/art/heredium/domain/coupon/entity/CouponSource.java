package art.heredium.domain.coupon.entity;

import art.heredium.domain.coupon.model.dto.request.*;
import lombok.Getter;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;

@Getter
public enum CouponSource {
  MEMBERSHIP_PACKAGE("멤버십"),
  ADMIN_SITE("관리자"),
  COMPANY("회사");

  private String desc;

  CouponSource(String desc) {
    this.desc = desc;
  }

  public static CouponSource fromCouponCreateRequest(CouponCreateRequest request) {
    if (request instanceof CompanyCouponCreateRequest) {
      return COMPANY;
    } else if (request instanceof MembershipCouponCreateRequest) {
      return MEMBERSHIP_PACKAGE;
    } else if (request instanceof NonMembershipCouponCreateRequest) {
      return ADMIN_SITE;
    } else if (request instanceof RecurringCouponCreateRequest) {
      return ADMIN_SITE; // 또는 필요시 새로운 ENUM 타입 추가
    } else {
      // This case will never happen
      throw new ApiException(
          ErrorCode.BAD_VALID,
          String.format(
              "Invalid coupon create request: %s, has to be CompanyCouponCreateRequest, MembershipCouponCreateRequest or NonMembershipCouponCreateRequest.",
              request));
    }
  }
}
