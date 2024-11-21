package art.heredium.domain.membership.entity;

import lombok.Getter;

public enum PaymentStatus {
  PENDING("보류중"),
  COMPLETED("가입 완료"),
  REFUND("환불 완료"),
  EXPIRED("기간 만료"),
  ;

  @Getter private String desc;

  PaymentStatus(final String desc) {
    this.desc = desc;
  }
}
