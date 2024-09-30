package art.heredium.domain.common.type;


import lombok.Getter;

@Getter
public enum HallType {
    A(0, "A"),
    B(1, "B"),
    C(2, "C"),
    ;

    private int code;
    private String desc;

    HallType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
