package art.heredium.domain.ticket.type;

import lombok.Getter;

import art.heredium.domain.common.converter.GenericTypeConverter;
import art.heredium.domain.common.type.PersistableEnum;

@Getter
public enum TicketType implements PersistableEnum<Integer> {
  NORMAL(0, "입장권"),
  GROUP(1, "단체 입장권"),
  INVITE(2, "초대권"),
  ;

  private int code;
  private String desc;

  TicketType(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  @Override
  public Integer getValue() {
    return this.code;
  }

  public static class Converter extends GenericTypeConverter<TicketType, Integer> {
    public Converter() {
      super(TicketType.class);
    }
  }
}
