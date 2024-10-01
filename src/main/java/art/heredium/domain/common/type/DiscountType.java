package art.heredium.domain.common.type;

import lombok.Getter;

import art.heredium.domain.common.converter.GenericTypeConverter;

@Getter
public enum DiscountType implements PersistableEnum<Integer> {
  HANA_BANK(0, "하나 은행"),
  ;

  private int code;
  private String desc;

  DiscountType(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  @Override
  public Integer getValue() {
    return this.code;
  }

  public static class Converter extends GenericTypeConverter<DiscountType, Integer> {
    public Converter() {
      super(DiscountType.class);
    }
  }
}
