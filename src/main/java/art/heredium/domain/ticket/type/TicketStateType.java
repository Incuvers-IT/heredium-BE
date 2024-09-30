package art.heredium.domain.ticket.type;


import art.heredium.domain.common.converter.GenericTypeConverter;
import art.heredium.domain.common.type.PersistableEnum;
import lombok.Getter;

@Getter
public enum TicketStateType implements PersistableEnum<Integer> {
    PAYMENT(0, "결제"),
    ISSUANCE(1, "발급"),
    USER_REFUND(2, "회원 환불"),
    ADMIN_REFUND(3, "관리자 환불"),
    USED(4, "사용 완료"),
    EXPIRED(5, "기간 만료"),
    ;

    private int code;
    private String desc;

    TicketStateType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return this.code;
    }

    public static class Converter extends GenericTypeConverter<TicketStateType, Integer> {
        public Converter() {
            super(TicketStateType.class);
        }
    }
}
