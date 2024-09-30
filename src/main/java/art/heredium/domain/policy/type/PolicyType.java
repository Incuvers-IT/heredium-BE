package art.heredium.domain.policy.type;


import art.heredium.domain.common.converter.GenericTypeConverter;
import art.heredium.domain.common.type.PersistableEnum;
import lombok.Getter;

@Getter
public enum PolicyType implements PersistableEnum<Integer> {
    PRIVACY(0, "개인정보처리방침"),
    SERVICE(1, "서비스 이용 약관"),
    AGREE(2, "개인정보 수집 및 이용 동의서"),
    REFUND(3, "취소 및 환불 정책"),
    VIDEO(4, "영상정보처리기기 운영 관리방침"),
    ;

    private int code;
    private String desc;

    PolicyType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return this.code;
    }

    public static class Converter extends GenericTypeConverter<PolicyType, Integer> {
        public Converter() {
            super(PolicyType.class);
        }
    }
}