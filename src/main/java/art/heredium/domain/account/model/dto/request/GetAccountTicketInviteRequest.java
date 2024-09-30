package art.heredium.domain.account.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GetAccountTicketInviteRequest {
    @NotNull
    private SearchDateType searchDateType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isLocalResident;
    @NotNull
    private Long minVisit, maxVisit;
    @NotNull
    private Long minInvite, maxInvite;
    private String text;
    private List<Long> excludeIds;


    @Getter
    public enum SearchDateType {
        CREATED_DATE(0, "가입일"),
        LAST_LOGIN_DATE(1, "최근 로그인"),
        ;

        private int code;
        private String desc;

        SearchDateType(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }
}