package art.heredium.domain.membership.entity;

public enum PaymentStatus {
  COMPLETED("완전한"),
  REFUND("환불하다"),
  EXPIRED("만료됨"),
  ;

  private String desc;

  PaymentStatus(final String desc) {
    this.desc = desc;
  }
}
