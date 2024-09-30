package art.heredium.domain.ticket.model.dto.request;

import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import art.heredium.domain.ticket.type.TicketType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GetAdminTicketRequest {
    @NotNull
    private SearchDateType searchDateType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private TicketType type;
    private List<TicketKindType> kinds = new ArrayList<>();
    private List<TicketStateType> state = new ArrayList<>();
    private String text;


    @Getter
    public enum SearchDateType {
        CREATED_DATE(0, "결제일시"),
        START_DATE(1, "회차시작"),
        USED_DATE(2, "사용일시"),
        ;

        private int code;
        private String desc;

        SearchDateType(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }
}