package art.heredium.domain.coupon.entity;

import lombok.Getter;

@Getter
public enum CouponType {
  EXHIBITION("전시", 0),
  PROGRAM("프로그램", 1),
  COFFEE("커피", 2),
  ARTSHOP("아트숍", 3),
  ;

  private String desc;
  private final int sortOrder;

  CouponType(String desc, int sortOrder) {
    this.desc = desc;
    this.sortOrder = sortOrder;
  }
}
