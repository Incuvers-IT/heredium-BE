package art.heredium.ncloud.type;

import lombok.Getter;

@Getter
public enum AlimTalkTemplate {
  SIGN_UP("회원가입 안내", "@heredium", "HEREDIUM001", "@spadecompany", "HEREDIUM001"),
  TICKET_ISSUANCE("예매 완료", "@heredium", "HEREDIUM002", "@spadecompany", "HEREDIUM015"),
  TICKET_INFORMATION("입장 안내", "@heredium", "HEREDIUM003", "@spadecompany", "HEREDIUM003"),
  TICKET_REFUND_USER("예매 취소", "@heredium", "HEREDIUM004", "@spadecompany", "HEREDIUM004"),
  TICKET_REFUND_ADMIN("예매 취소", "@heredium", "HEREDIUM005", "@spadecompany", "HEREDIUM014"),
  TICKET_ISSUANCE_GROUP("단체입장권 발급", "@heredium", "HEREDIUM006", "@spadecompany", "HEREDIUM006"),
  TICKET_REFUND_GROUP("단체입장권 취소", "@heredium", "HEREDIUM007", "@spadecompany", "HEREDIUM007"),
  TICKET_INVITE("초대권 발급", "@heredium", "HEREDIUM008", "@spadecompany", "HEREDIUM016"),
  ACCOUNT_SLEEP("휴면계정 안내", "@heredium", "HEREDIUM009", "@spadecompany", "HEREDIUM009"),
  ACCOUNT_NOTY_SLEEP_TERMINATE(
      "자동탈퇴 안내", "@heredium", "HEREDIUM010", "@spadecompany", "HEREDIUM010"),
  ACCOUNT_SLEEP_TERMINATE("자동탈퇴 완료", "@heredium", "HEREDIUM011", "@spadecompany", "HEREDIUM011"),
  ACCOUNT_TERMINATE("회원탈퇴 안내", "@heredium", "HEREDIUM012", "@spadecompany", "HEREDIUM012"),
  COFFEE_COMPLETE("커피제작 완료", "@heredium", "HEREDIUM013", "@spadecompany", "HEREDIUM013"),
  USER_REGISTER_MEMBERSHIP_PACKAGE(
      "템플릿 이름",
      "@heredium",
      "HEREDIUM021",
      "@spadecompany",
      ""), // dev templateCode has not been created
  MEMBERSHIP_PACKAGE_HAS_EXPIRED(
      "멤버십 패키지가 만료되었습니다",
      "@heredium",
      "HEREDIUM020",
      "@spadecompany",
      ""), // dev templateCode has not been created
  COUPON_HAS_BEEN_ISSUED_V3(
      "쿠폰발행",
      "@heredium",
      "HEREDIUM026",
      "@spadecompany",
      ""), // Replaced for COUPON_HAS_BEEN_DELIVERED
  NON_MEMBERSHIP_COUPON_HAS_BEEN_USED(
      "", "@heredium", "HEREDIUM023", "@spadecompany", ""), // dev templateCode has not been created
  WITH_MEMBERSHIP_COUPON_HAS_BEEN_USED(
      "", "@heredium", "HEREDIUM024", "@spadecompany", ""), // dev templateCode has not been created
  ;

  private String prodPlusFriendId;
  private String prodTemplateCode;
  private String devPlusFriendId;
  private String devTemplateCode;
  private String title;

  AlimTalkTemplate(
      String title,
      String prodPlusFriendId,
      String prodTemplateCode,
      String devPlusFriendId,
      String devTemplateCode) {
    this.title = title;
    this.prodPlusFriendId = prodPlusFriendId;
    this.prodTemplateCode = prodTemplateCode;
    this.devPlusFriendId = devPlusFriendId;
    this.devTemplateCode = devTemplateCode;
  }

  public String getPlusFriendId(String active) {
    if (active.equals("prod")) {
      return prodPlusFriendId;
    } else {
      return devPlusFriendId;
    }
  }

  public String getTemplateCode(String active) {
    if (active.equals("prod")) {
      return prodTemplateCode;
    } else {
      return devTemplateCode;
    }
  }
}
