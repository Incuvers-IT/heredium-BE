package art.heredium.core.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public final class DateTimeUtil {
    private DateTimeUtil() {
        super();
    }

    public static LocalDateTime toZone(final LocalDateTime time, final ZoneId fromZone, final ZoneId toZone) {
        final ZonedDateTime zonedtime = time.atZone(fromZone);
        final ZonedDateTime converted = zonedtime.withZoneSameInstant(toZone);
        return converted.toLocalDateTime();
    }

    public static LocalDateTime toZone(final LocalDateTime time, final ZoneId zoneId) {
        return DateTimeUtil.toZone(time, ZoneId.systemDefault(), zoneId);
    }

    public static LocalDateTime toUtc(final LocalDateTime time, final ZoneId fromZone) {
        return DateTimeUtil.toZone(time, fromZone, ZoneOffset.UTC);
    }

    public static LocalDateTime kstToUtc(final LocalDateTime time) {
        return DateTimeUtil.toUtc(time, ZoneId.of("Asia/Seoul"));
    }

    public static LocalDateTime utcToKst(final LocalDateTime time) {
        return DateTimeUtil.toZone(time, ZoneId.of("Asia/Seoul"));
    }

    public static LocalDateTime nowToUtc(String timeZoneName) {
        ZoneId zoneId = ZoneId.of(timeZoneName);
        LocalDateTime time = LocalDateTime.now(zoneId);
        return DateTimeUtil.toUtc(time, zoneId);
    }

    public static LocalDateTime toUtc(final LocalDateTime time) {
        return DateTimeUtil.toUtc(time, ZoneId.systemDefault());
    }
}
