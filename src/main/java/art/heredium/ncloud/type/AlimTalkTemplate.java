package art.heredium.ncloud.type;

import lombok.Getter;

@Getter
public enum AlimTalkTemplate {
  // 회원가입
  SIGN_UP("", "@heredium", "HEREDIUM046", "@heredium", "HEREDIUM046"),
  // 예약완료
  TICKET_ISSUANCE("", "@heredium", "HEREDIUM036", "@heredium", "HEREDIUM036"),
  // 입장 안내
  TICKET_INFORMATION("", "@heredium", "HEREDIUM037", "@heredium", "HEREDIUM037"),
  // 애매 취소
  TICKET_REFUND_USER("", "@heredium", "HEREDIUM038", "@heredium", "HEREDIUM038"),
  // 애매 취소 관리자
  TICKET_REFUND_ADMIN("", "@heredium", "HEREDIUM039", "@heredium", "HEREDIUM039"),
  // 단체 입장권 발급(휴면상태)
  TICKET_ISSUANCE_GROUP("", "@heredium", "HEREDIUM051", "@heredium", "HEREDIUM051"),
  // 단체 입장권 취소(휴면상태)
  TICKET_REFUND_GROUP("", "@heredium", "HEREDIUM052", "@heredium", "HEREDIUM052"),
  // 초대권 발급
  TICKET_INVITE("", "@heredium", "HEREDIUM040", "@heredium", "HEREDIUM040"),
  // 휴면계정(휴면상태)
  ACCOUNT_SLEEP("휴면계정 안내", "@heredium", "HEREDIUM009", "@heredium", "HEREDIUM009"),
  // 자동탈퇴 안내(휴면)
  ACCOUNT_NOTY_SLEEP_TERMINATE(
      "자동탈퇴 안내", "@heredium", "HEREDIUM010", "@heredium", "HEREDIUM010"),
  // 자동탈퇴 완료(휴면)
  ACCOUNT_SLEEP_TERMINATE("자동탈퇴 완료", "@heredium", "HEREDIUM011", "@heredium", "HEREDIUM011"),
  // 회원탈퇴 안내
  ACCOUNT_TERMINATE("", "@heredium", "HEREDIUM041", "@heredium", "HEREDIUM041"),
  // 커피제작 완료(휴면)
  COFFEE_COMPLETE("커피제작 완료", "@heredium", "HEREDIUM013", "@heredium", "HEREDIUM013"),
  // 쿠폰 발급
  COUPON_HAS_BEEN_ISSUED_V4(
      "",
      "@heredium",
      "HEREDIUM047",
      "@heredium",
      "HEREDIUM047"),
  // 멤버십 승급 안내 1>2
  TIER_UPGRADE(
          "", "@heredium", "HEREDIUM042", "@heredium", "HEREDIUM042"),
  // 멤버십 기한 만료 안내
  MEMBERSHIP_EXPIRY_REMINDER(
          "", "@heredium", "HEREDIUM049", "@heredium", "HEREDIUM049"),
  // 등급 조정 안내 2>1 환불 또는 결제취소에 의한 멤버십 등급 롤백
  MEMBERSHIP_TIER_REFUND(
          "", "@heredium", "HEREDIUM050", "@heredium", "HEREDIUM050"),
  // 멤버십 등급 전환 3 > 1 || 2 > 1로 전환시
  MEMBERSHIP_TIER_DEMOTED(
          "", "@heredium", "HEREDIUM048", "@heredium", "HEREDIUM048")
  ;

  private final String prodPlusFriendId;
  private final String prodTemplateCode;
  private final String devPlusFriendId;
  private final String devTemplateCode;
  private final String title;

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
