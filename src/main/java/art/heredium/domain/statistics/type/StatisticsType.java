package art.heredium.domain.statistics.type;

import lombok.Getter;

@Getter
public enum StatisticsType {
  COME(0, "입장"),
  PRICE(1, "매출"),
  SIGN_UP(2, "회원가입"),
  ;

  private int code;
  private String desc;

  StatisticsType(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }
}
