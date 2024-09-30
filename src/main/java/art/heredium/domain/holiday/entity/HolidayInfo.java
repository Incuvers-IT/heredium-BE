package art.heredium.domain.holiday.entity;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "holiday_config")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
//휴일 config
public class HolidayInfo implements Serializable {
    private static final long serialVersionUID = -9085923330483905478L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("마지막으로 선택한 요일")
    @Type(type = "json")
    @Column(name = "days", columnDefinition = "json", nullable = false)
    private List<Integer> days = new ArrayList<>(); //0 : 월요일 ~ 6 : 일요일

    public HolidayInfo(List<Integer> days) {
        this.days = days;
    }

    public void updateDays(List<Integer> days) {
        this.days = days;
    }
}
