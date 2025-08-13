package art.heredium.ncloud.type;

import lombok.Getter;

@Getter
public enum AlimTalkTemplate {
  SIGN_UP("", "@heredium", "HEREDIUM001", "@heredium", "HEREDIUM035"), //템플릿 검수 완료시 HEREDIUM046 사용
  TICKET_ISSUANCE("", "@heredium", "HEREDIUM002", "@heredium", "HEREDIUM036"),
  TICKET_INFORMATION("", "@heredium", "HEREDIUM003", "@heredium", "HEREDIUM037"),
  TICKET_REFUND_USER("", "@heredium", "HEREDIUM004", "@heredium", "HEREDIUM038"),
  TICKET_REFUND_ADMIN("예매 취소", "@heredium", "HEREDIUM005", "@heredium", "HEREDIUM039"),
  TICKET_ISSUANCE_GROUP("단체입장권 발급", "@heredium", "HEREDIUM006", "@heredium", "HEREDIUM006"), //휴면
  TICKET_REFUND_GROUP("단체입장권 취소", "@heredium", "HEREDIUM007", "@heredium", "HEREDIUM007"), //휴면
  TICKET_INVITE("초대권 발급", "@heredium", "HEREDIUM008", "@heredium", "HEREDIUM008"), //기존 템플릿 유지
  ACCOUNT_SLEEP("휴면계정 안내", "@heredium", "HEREDIUM009", "@heredium", "HEREDIUM009"), // 휴면
  ACCOUNT_NOTY_SLEEP_TERMINATE(
      "자동탈퇴 안내", "@heredium", "HEREDIUM010", "@heredium", "HEREDIUM010"), //휴면
  ACCOUNT_SLEEP_TERMINATE("자동탈퇴 완료", "@heredium", "HEREDIUM011", "@heredium", "HEREDIUM011"), //휴면
  ACCOUNT_TERMINATE("", "@heredium", "HEREDIUM012", "@heredium", "HEREDIUM041"),
  COFFEE_COMPLETE("커피제작 완료", "@heredium", "HEREDIUM013", "@heredium", "HEREDIUM013"), //휴면
  USER_REGISTER_MEMBERSHIP_PACKAGE(
      "템플릿 이름",
      "@heredium",
      "HEREDIUM021",
      "@spadecompany",
      ""), //삭제 // dev templateCode has not been created
  MEMBERSHIP_PACKAGE_HAS_EXPIRED(
      "멤버십 패키지가 만료되었습니다",
      "@heredium",
      "HEREDIUM020",
      "@spadecompany",
      ""), //삭제  // dev templateCode has not been created
  COUPON_HAS_BEEN_ISSUED_V4(
      "",
      "@heredium",
      "HEREDIUM027",
      "@heredium",
      "HEREDIUM031"), // Replaced for COUPON_HAS_BEEN_DELIVERED
  NON_MEMBERSHIP_COUPON_HAS_BEEN_USED(
      "", "@heredium", "HEREDIUM023", "@heredium", "HEREDIUM023"), //삭제 // dev templateCode has not been created
  WITH_MEMBERSHIP_COUPON_HAS_BEEN_USED(
      "", "@heredium", "HEREDIUM024", "@heredium", "HEREDIUM024"), //삭제 // dev templateCode has not been created
  TIER_UPGRADE(
          "", "@heredium", "HEREDIUM030", "@heredium", "HEREDIUM042"),
  MEMBERSHIP_EXPIRY_REMINDER(
          "", "@heredium", "HEREDIUM032", "@heredium", "HEREDIUM043")
  // 알림톡 추가 필요
  // 등급 조정 안내 HEREDIUM045 ; 환불 또는 결제취소에 의한 멤버십 등급 롤백
  // 멤버십 등급 전환 HEREDIUM044; 3 > 1 || 2 > 1로 전환시
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
