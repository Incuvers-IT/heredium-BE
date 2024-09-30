package art.heredium.domain.holiday.entity;

import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.log.entity.Log;
import art.heredium.domain.log.type.LogAction;
import art.heredium.domain.log.type.LogType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter
@Table(name = "holiday")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
//휴일
public class Holiday implements Serializable {
    private static final long serialVersionUID = 8916421905765770041L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("휴일 날짜")
    @Column(name = "day", nullable = false, unique = true)
    private LocalDate day;

    public Holiday(LocalDate date) {
        this.day = date;
    }

    public Log createInsertLog(Admin admin) {
        return new Log(admin, this.day.toString(), this.toString(), LogType.HOLIDAY, LogAction.INSERT);
    }

    public Log createUpdateLog(Admin admin) {
        return new Log(admin, this.day.toString(), this.toString(), LogType.HOLIDAY, LogAction.UPDATE);
    }

    public Log createDeleteLog(Admin admin) {
        return new Log(admin, this.day.toString(), this.toString(), LogType.HOLIDAY, LogAction.DELETE);
    }
}
