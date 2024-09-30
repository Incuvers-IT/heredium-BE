package art.heredium.payment.type;

import art.heredium.core.config.spring.ApplicationBeanUtil;
import art.heredium.domain.common.converter.GenericTypeConverter;
import art.heredium.domain.common.type.PersistableEnum;
import art.heredium.domain.ticket.entity.Ticket;
import art.heredium.payment.inf.PaymentService;
import art.heredium.payment.inicis.Inicis;
import art.heredium.payment.tosspayments.TossPayments;
import lombok.Getter;

@Getter
public enum PaymentType implements PersistableEnum<Integer> {

    //토스페이먼츠의 경우 모바일일때 app store, play store 수수료 관련 이슈로 인하여 pc와 모바일을 구분하여 각각의 secret key를 사용하여야함.
    TOSSPAYMENTS(0, "토스페이먼츠", TossPayments.class.getSimpleName(), "tosspayments.secret-key"),
    TOSSPAYMENTS_ANDROID(1, "토스페이먼츠 Android", TossPayments.class.getSimpleName(), "tosspayments.android-secret-key"),
    TOSSPAYMENTS_IOS(2, "토스페이먼츠 IOS", TossPayments.class.getSimpleName(), "tosspayments.ios-secret-key"),

    //이니시스는 1개만 사용
    INICIS(3, "이니시스", Inicis.class.getSimpleName(), "inicis.sign-key"),
    ;

    private int code;
    private String desc;
    private String simpleName;
    private String propertyKeyName;

    PaymentType(int code, String desc, String simpleName, String propertyKeyName) {
        this.code = code;
        this.desc = desc;
        this.simpleName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
        this.propertyKeyName = propertyKeyName;
    }

    @Override
    public Integer getValue() {
        return this.code;
    }

    public void refund(Ticket ticket) {
        PaymentService service = (PaymentService) ApplicationBeanUtil.getBean(this.getSimpleName());
        service.refund(ticket);
    }

    public static class Converter extends GenericTypeConverter<PaymentType, Integer> {
        public Converter() {
            super(PaymentType.class);
        }
    }
}
