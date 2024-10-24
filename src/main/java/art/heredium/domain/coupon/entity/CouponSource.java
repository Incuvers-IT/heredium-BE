package art.heredium.domain.coupon.entity;

import lombok.Getter;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.coupon.model.dto.request.CompanyCouponCreateRequest;
import art.heredium.domain.coupon.model.dto.request.CouponCreateRequest;
import art.heredium.domain.coupon.model.dto.request.MembershipCouponCreateRequest;
import art.heredium.domain.coupon.model.dto.request.NonMembershipCouponCreateRequest;

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
