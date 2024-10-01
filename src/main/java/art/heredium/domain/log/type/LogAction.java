package art.heredium.domain.log.type;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;

import art.heredium.domain.common.converter.GenericTypeConverter;
import art.heredium.domain.common.type.PersistableEnum;

@Getter
public enum LogAction implements PersistableEnum<Integer> {
  SUCCESS(0, "성공"),
  FAIL(1, "실패"),
  INSERT(2, "추가"),
  UPDATE(3, "수정"),
  DELETE(4, "삭제"),
  ;

  private int code;
  private String desc;

  LogAction(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  @Override
  public Integer getValue() {
    return this.code;
  }

  public static class Converter extends GenericTypeConverter<LogAction, Integer> {
    public Converter() {
      super(LogAction.class);
    }
  }

  public static List<Integer> toCodeArray(Set<LogAction> set) {
    return set.stream().map(LogAction::getCode).collect(Collectors.toList());
  }

  public static List<String> toNameArray(String name) {
    return Arrays.stream(LogAction.values())
        .map(LogAction::name)
        .filter(s -> s.startsWith(name))
        .collect(Collectors.toList());
  }
}
