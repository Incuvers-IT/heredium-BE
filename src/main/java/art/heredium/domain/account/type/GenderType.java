package art.heredium.domain.account.type;

import lombok.Getter;

import art.heredium.domain.common.converter.GenericTypeConverter;
import art.heredium.domain.common.type.PersistableEnum;

@Getter
public enum GenderType implements PersistableEnum<Integer> {
  MAN(0, "남"),
  WOMAN(1, "여"),
  ;

  private int code;
  private String desc;

  GenderType(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  @Override
  public Integer getValue() {
    return this.code;
  }

  public static class Converter extends GenericTypeConverter<GenderType, Integer> {
    public Converter() {
      super(GenderType.class);
    }
  }
}
