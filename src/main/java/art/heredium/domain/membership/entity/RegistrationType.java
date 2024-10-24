package art.heredium.domain.membership.entity;

public enum RegistrationType {
  MEMBERSHIP_PACKAGE("멤버십"),
  COMPANY("회사"),
  ;

  private String desc;

  RegistrationType(String desc) {
    this.desc = desc;
  }
}
