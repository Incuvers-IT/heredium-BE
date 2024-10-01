package art.heredium.domain.log.type;

import lombok.Getter;

@Getter
public enum LogSearchType {
  ALL(0, "전체"),
  EMAIL(1, "아이디"),
  NAME(2, "이름"),
  TYPE(3, "구분"),
  ;

  private int code;
  private String desc;

  LogSearchType(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }
}
