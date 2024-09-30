package art.heredium.domain.common.type;


import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum ProjectPriceType {
    ADULT(0, "성인", true),
    TEENAGER(1, "청소년", true),
    CHILDREN(2, "유아 및 어린이", true),
    GROUP(3, "단체", false),
    INVITE(4, "초대", false),
    ;

    private int code;
    private String desc;
    private boolean isDefault;

    ProjectPriceType(int code, String desc, boolean isDefault) {
        this.code = code;
        this.desc = desc;
        this.isDefault = isDefault;
    }

    public static List<ProjectPriceType> getDefault() {
        return Arrays.stream(values()).filter(value -> value.isDefault).collect(Collectors.toList());
    }

    public static Integer getCodeOfDesc(String value) {
        ProjectPriceType projectPriceType = Arrays.stream(ProjectPriceType.values())
                .filter(v -> v.getDesc().equals(value))
                .findAny()
                .orElse(null);
        return projectPriceType != null ? projectPriceType.getCode() : null;
    }
}
