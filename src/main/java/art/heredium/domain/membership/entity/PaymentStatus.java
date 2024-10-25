package art.heredium.domain.membership.entity;

import lombok.Getter;

public enum PaymentStatus {
  COMPLETED("완전한"),
  REFUND("환불하다"),
  EXPIRED("만료됨"),
  ;

  @Getter private String desc;

  PaymentStatus(final String desc) {
    this.desc = desc;
  }
}
