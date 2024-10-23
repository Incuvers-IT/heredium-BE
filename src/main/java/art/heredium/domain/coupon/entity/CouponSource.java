package art.heredium.domain.coupon.entity;

import lombok.Getter;

@Getter
public enum CouponSource {
  MEMBERSHIP_PACKAGE("멤버십"),
  ADMIN_SITE("관리자"),
  ;

  private String desc;

  CouponSource(String desc) {
    this.desc = desc;
  }
}
