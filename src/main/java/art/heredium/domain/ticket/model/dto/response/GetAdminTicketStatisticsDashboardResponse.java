package art.heredium.domain.ticket.model.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAdminTicketStatisticsDashboardResponse {
    private String type;
    private Integer number;
    private Long price;

    @QueryProjection
    public GetAdminTicketStatisticsDashboardResponse(String type, Integer number, Long price) {
        this.type = type;
        this.number = number;
        this.price = price;
    }
}
