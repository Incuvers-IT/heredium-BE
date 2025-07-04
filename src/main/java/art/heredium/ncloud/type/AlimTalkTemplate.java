package art.heredium.ncloud.type;

import lombok.Getter;

@Getter
public enum AlimTalkTemplate {
  SIGN_UP("회원가입 안내", "@heredium", "HEREDIUM001", "@heredium", "HEREDIUM028"),
  TICKET_ISSUANCE("예매 완료", "@heredium", "HEREDIUM002", "@heredium", "HEREDIUM002"),
  TICKET_INFORMATION("입장 안내", "@heredium", "HEREDIUM003", "@heredium", "HEREDIUM003"),
  TICKET_REFUND_USER("예매 취소", "@heredium", "HEREDIUM004", "@heredium", "HEREDIUM004"),
  TICKET_REFUND_ADMIN("예매 취소", "@heredium", "HEREDIUM005", "@heredium", "HEREDIUM005"),
  TICKET_ISSUANCE_GROUP("단체입장권 발급", "@heredium", "HEREDIUM006", "@heredium", "HEREDIUM006"),
  TICKET_REFUND_GROUP("단체입장권 취소", "@heredium", "HEREDIUM007", "@heredium", "HEREDIUM007"),
  TICKET_INVITE("초대권 발급", "@heredium", "HEREDIUM008", "@heredium", "HEREDIUM008"),
  ACCOUNT_SLEEP("휴면계정 안내", "@heredium", "HEREDIUM009", "@heredium", "HEREDIUM009"),
  ACCOUNT_NOTY_SLEEP_TERMINATE(
      "자동탈퇴 안내", "@heredium", "HEREDIUM010", "@heredium", "HEREDIUM010"),
  ACCOUNT_SLEEP_TERMINATE("자동탈퇴 완료", "@heredium", "HEREDIUM011", "@heredium", "HEREDIUM011"),
  ACCOUNT_TERMINATE("회원탈퇴 안내", "@heredium", "HEREDIUM012", "@heredium", "HEREDIUM012"),
  COFFEE_COMPLETE("커피제작 완료", "@heredium", "HEREDIUM013", "@heredium", "HEREDIUM013"),
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
  COUPON_HAS_BEEN_ISSUED_V4(
      "쿠폰발행",
      "@heredium",
      "HEREDIUM027",
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
