package art.heredium.domain.log.type;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;

import art.heredium.domain.common.converter.GenericTypeConverter;
import art.heredium.domain.common.type.PersistableEnum;

@Getter
public enum LogType implements PersistableEnum<Integer> {
  LOGIN(0, "로그인"),
  ACCOUNT(1, "마이페이지"),
  EXHIBITION(2, "전시"),
  PROGRAM(3, "프로그램"),
  TICKET(4, "티켓"),
  HOLIDAY(5, "휴일"),
  POPUP(6, "팝업 관리"),
  DOCENT(7, "도센트"),
  ADMIN(8, "관리자정보"),
  POLICY(9, "개인정보처리방침"),
  SLIDE(10, "슬라이드"),
  NOTICE(11, "공지사항"),
  EVENT(12, "이벤트"),
  COFFEE(13, "커피"),
  ;

  private int code;
  private String desc;

  LogType(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  @Override
  public Integer getValue() {
    return this.code;
  }

  public static class Converter extends GenericTypeConverter<LogType, Integer> {
    public Converter() {
      super(LogType.class);
    }
  }

  public static LogType containsDesc(String name) {
    return Arrays.stream(LogType.values())
        .filter(v -> v.desc.contains(name))
        .findAny()
        .orElse(null);
  }

  public static List<Integer> toCodeArray(Set<LogType> set) {
    return set.stream().map(LogType::getCode).collect(Collectors.toList());
  }

  public static List<String> toNameArray(String name) {
    return Arrays.stream(LogType.values())
        .map(LogType::name)
        .filter(s -> s.startsWith(name))
        .collect(Collectors.toList());
  }
}
