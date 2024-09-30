package art.heredium.domain.coffee.model.dto.request;

import art.heredium.domain.common.type.HallType;
import art.heredium.domain.common.type.ProjectStateType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GetAdminCoffeeRequest {
    @NotNull
    private SearchDateType searchDateType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private HallType hall;
    private Boolean isEnabled;
    private List<ProjectStateType> state = new ArrayList<>();
    private String text;


    @Getter
    public enum SearchDateType {
        CREATED_DATE(0, "등록일"),
        LAST_MODIFIED_DATE(1, "수정일"),
        SCHEDULE(2, "일정"),
        ;

        private int code;
        private String desc;

        SearchDateType(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }
}