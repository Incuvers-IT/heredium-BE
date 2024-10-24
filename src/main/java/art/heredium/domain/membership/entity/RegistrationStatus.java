package art.heredium.domain.membership.entity;

public enum RegistrationStatus {
  SUCCESS("성공"),
  FAILED("실패"),
  ;

  private String desc;

  RegistrationStatus(String desc) {
    this.desc = desc;
  }
}
