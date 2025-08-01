package art.heredium.ncloud.type;

import lombok.Getter;

@Getter
public enum MailTemplate {
  SIGN_UP(8154, 8154, "회원가입"),
  PASSWORD_CHANGE_USER(8119, 7551, "비밀번호 변경(회원)"),
  PASSWORD_CHANGE_ADMIN(8152, 8179, "비밀번호 변경(관리자)"),
  TICKET_ISSUANCE(8120, 8120, "입장권 발급 완료"),
  TICKET_REFUND_USER(8121, 7553, "입장권 취소(회원)"),
  TICKET_REFUND_ADMIN(8122, 7554, "입장권 취소(관리자)"),
  TICKET_ISSUANCE_GROUP(8123, 7555, "단체입장권 발급"),
  TICKET_REFUND_GROUP(8124, 7556, "단체입장권 취소"),
  TICKET_INVITE(8125, 7557, "초대권 발급"),
  ACCOUNT_SLEEP(8126, 7558, "휴면계정 변환"),
  ACCOUNT_NOTY_SLEEP_TERMINATE(8127, 7559, "자동탈퇴안내"),
  ACCOUNT_SLEEP_TERMINATE(8155, 8158, "자동탈퇴"),
  ACCOUNT_TERMINATE(8156, 8159, "회원탈퇴"),
  REQUEST_GROUP(8128, 8021, "단체입장권 신청"),
  ;

  private int prodId;
  private int devId;
  private String key;

  MailTemplate(int prodId, int devId, String key) {
    this.prodId = prodId;
    this.devId = devId;
    this.key = key;
  }

  public int getId(String active) {
    if (active.equals("prod")) {
      return prodId;
    } else {
      return devId;
    }
  }
}
