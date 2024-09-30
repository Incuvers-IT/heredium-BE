package art.heredium.domain.ticket.type;


import art.heredium.domain.common.converter.GenericTypeConverter;
import art.heredium.domain.common.type.PersistableEnum;
import lombok.Getter;

@Getter
public enum TicketKindType implements PersistableEnum<Integer> {
    EXHIBITION(0, "전시"),
    PROGRAM(1, "프로그램"),
    COFFEE(2, "커피주문"),
    ;

    private int code;
    private String desc;

    TicketKindType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return this.code;
    }

    public static class Converter extends GenericTypeConverter<TicketKindType, Integer> {
        public Converter() {
            super(TicketKindType.class);
        }
    }
}