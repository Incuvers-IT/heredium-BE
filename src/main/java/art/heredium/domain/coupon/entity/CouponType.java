package art.heredium.domain.coupon.entity;

public enum CouponType {
  DRINK("마시다"),
  ARTSHOP("아트숍"),
  PROGRAM("프로그램"),
  EXHIBITION("전시"),
  ;

  private String desc;

  CouponType(String desc) {
    this.desc = desc;
  }
}
