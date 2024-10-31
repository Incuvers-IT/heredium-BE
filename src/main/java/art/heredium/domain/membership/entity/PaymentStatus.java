package art.heredium.domain.membership.entity;

import lombok.Getter;

public enum PaymentStatus {
  IGNORED("무시하다"),
  PENDING("보류중"),
  COMPLETED("완전한"),
  REFUND("환불하다"),
  EXPIRED("만료됨"),
  ;

  @Getter private String desc;

  PaymentStatus(final String desc) {
    this.desc = desc;
  }
}
