package art.heredium.domain.coupon.entity;

public enum CouponType {
  COFFEE("커피"),
  TICKET("티켓"),
  PROGRAM("프로그램"),
  EXHIBITION("전시"),
  ;

  private String desc;

  CouponType(String desc) {
    this.desc = desc;
  }
}
