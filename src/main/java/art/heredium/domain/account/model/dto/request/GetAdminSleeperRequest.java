package art.heredium.domain.account.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class GetAdminSleeperRequest {
    @NotNull
    private SearchDateType searchDateType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isMarketingReceive;
    private String text;


    @Getter
    public enum SearchDateType {
        SLEEP_DATE(0, "휴면 전환 일시"),
        CREATED_DATE(1, "가입일시"),
        ;

        private int code;
        private String desc;

        SearchDateType(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }
}
