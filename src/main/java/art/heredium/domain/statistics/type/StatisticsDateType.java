package art.heredium.domain.statistics.type;

import lombok.Getter;

@Getter
public enum StatisticsDateType {
    DAY(0, "Day", "%Y-%m-%d"),
    MONTH(1, "Month", "%Y-%m"),
    DAY_WEEK(2, "Month", "%w"),
    HOUR(3, "Month", "%H"),
    ;

    private int code;
    private String desc;
    private String format;

    StatisticsDateType(int code, String desc, String format) {
        this.code = code;
        this.desc = desc;
        this.format = format;
    }
}