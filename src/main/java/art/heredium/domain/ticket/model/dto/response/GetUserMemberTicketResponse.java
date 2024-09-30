package art.heredium.domain.ticket.model.dto.response;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketStateType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class GetUserMemberTicketResponse {
    private Long id;
    private Storage thumbnail;
    private String title;
    private String uuid;
    private TicketKindType kind;
    private TicketStateType state;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Info> info;

    @Getter
    @Setter
    private static class Info {
        private String type;
        private Integer number;

        private Info(String info) {
            // info : {type}-{number}
            String[] splitInfo = info.split("=-=-=-=");
            this.type = splitInfo[0];
            this.number = Integer.valueOf(splitInfo[1]);
        }
    }

    @QueryProjection
    public GetUserMemberTicketResponse(Long id, Storage thumbnail, String title, String uuid, TicketKindType kind, TicketStateType state, LocalDateTime startDate, LocalDateTime endDate, String info) {
        this.id = id;
        this.thumbnail = thumbnail;
        this.title = title;
        this.uuid = uuid;
        this.kind = kind;
        this.state = state;
        this.startDate = startDate;
        this.endDate = endDate;
        String[] infos = info.split("=-,-=");
        this.info = Arrays.stream(infos).map(Info::new).collect(Collectors.toList());
    }
}