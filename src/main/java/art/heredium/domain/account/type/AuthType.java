package art.heredium.domain.account.type;


import art.heredium.domain.common.converter.GenericTypeConverter;
import art.heredium.domain.common.type.PersistableEnum;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum AuthType implements PersistableEnum<Integer> {
    ADMIN(0, "최고 관리자"),
    SUPERVISOR(1, "운영 관리자"),
    MANAGER(2, "일반 관리자"),
    COFFEE(3, "카페 직원"),
    USER(4, "유저"),
    ;

    private int code;
    private String desc;

    AuthType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public Integer getValue() {
        return this.code;
    }

    public static class Converter extends GenericTypeConverter<AuthType, Integer> {
        public Converter() {
            super(AuthType.class);
        }
    }

    public String getRole() {
        return "ROLE_" + name();
    }

    public String getName() {
        return name();
    }

    public static String[] getAPIRole() {
        return Arrays
                .stream(values())
                .map(Enum::name)
                .toArray(String[]::new);
    }
}
