package art.heredium.ncloud.type;

import lombok.Getter;

@Getter
public enum SmsTemplate {
  TICKET_ISSUANCE(
      "입장권 발급 완료",
      "헤레디움 입장권 발급 완료",
      "헤레디움\n"
          + "${title} 입장권이 발급되었어요!\n"
          + "${startDate}\n"
          + "${price}원\n"
          + "${info}\n\n"
          + "헤레디움 앱에 있는 QR입장권으로 관람을 시작해보세요.\n\n"
          + "곧 만나요!\n"),
  TICKET_REFUND_USER(
      "입장권 취소(회원)",
      "헤레디움 입장권 취소",
      "헤레디움\n"
          + "${title} 입장권이 취소되었습니다.\n"
          + "\n"
          + "${startDate}\n"
          + "${price}원\n"
          + "${info}\n\n"
          + "환불된 금액은 영업일 기준 3~5일 후에 카드사에 확인해보세요.\n\n"
          + "카드사 관련 문의는 개인 정보 확인 절차로 헤레디움에서 확인해드릴 수 없어요.\n\n"
          + "궁금한 사항은 고객센터에서 해결해 드릴게요.\n"
          + "전화 : {전화번호} | 이메일 : {이메일}\n"),
  TICKET_REFUND_ADMIN(
      "입장권 취소(관리자)",
      "헤레디움 입장권 취소",
      "헤레디움\n"
          + "${title} 입장권이 취소되었습니다.\n\n"
          + "${startDate}\n"
          + "${price}원\n"
          + "${info}\n\n"
          + "환불된 금액은 영업일 기준 3~5일 후에 카드사에 확인해보세요.\n\n"
          + "카드사 관련 문의는 개인 정보 확인 절차로 헤레디움에서 확인해드릴 수 없어요.\n\n"
          + "궁금한 사항은 고객센터에서 해결해 드릴게요.\n"
          + "전화 : {전화번호} | 이메일 : {이메일}"),
  TICKET_ISSUANCE_GROUP(
      "단체입장권 발급",
      "헤레디움 단체입장권 발급",
      "헤레디움\n"
          + "${title} 단체입장권이 발급되었어요!\n\n"
          + "${startDate}\n"
          + "${price}원\n"
          + "${info}\n\n"
          + "헤레디움 앱에 있는 QR입장권으로 관람을 시작해보세요,\n\n"
          + "곧 만나요!\n"),
  TICKET_REFUND_GROUP(
      "단체입장권 취소",
      "헤레디움 단체입장권 취소",
      "헤레디움\n"
          + "${title} 단체입장권이 취소 되었어요.\n\n"
          + "${startDate}\n"
          + "${price}원\n"
          + "${info}\n\n"
          + "환불 금액이 입금되지 않으면 고객센터로 연락해 주세요.\n"
          + "전화 : {전화번호} | 이메일 : {이메일}\n"),
  TICKET_INVITE(
      "초대권 발급",
      "헤레디움 초대권 발급",
      "헤레디움\n"
          + "${title} 초대권이 발급되었어요!\n"
          + "\n"
          + "${startDate}\n"
          + "${info}\n\n"
          + "헤레디움 앱에 있는 QR입장권으로 관람을 시작해보세요.\n\n"
          + "곧 만나요!\n"),
  COFFEE_COMPLETE("커피 제작 완료", "커피 제작 완료", "헤레디움\n" + "${info} 제작이 완료되었습니다."),
  ;

  private String key;
  private String title;
  private String contents;

  SmsTemplate(String key, String title, String contents) {
    this.key = key;
    this.title = title;
    this.contents = contents;
  }
}
