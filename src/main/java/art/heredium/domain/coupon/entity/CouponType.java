package art.heredium.domain.coupon.entity;

import lombok.Getter;

@Getter
public enum CouponType {
  COFFEE("커피"),
  ARTSHOP("아트숍"),
  PROGRAM("프로그램"),
  EXHIBITION("전시"),
  ;

  private String desc;

  CouponType(String desc) {
    this.desc = desc;
  }
}
