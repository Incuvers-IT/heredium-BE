package art.heredium.domain.ticket.model.dto.response;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import com.querydsl.core.annotations.QueryProjection;

import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.domain.ticket.type.TicketType;

@Getter
@Setter
public class GetUserMemberTicketEnabledResponse {
  private Long id;
  private String title;
  private TicketKindType kind;
  private TicketType type;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private String uuid;
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
  public GetUserMemberTicketEnabledResponse(
      Long id,
      String title,
      TicketKindType kind,
      TicketType type,
      LocalDateTime startDate,
      LocalDateTime endDate,
      String uuid,
      String info) {
    this.id = id;
    this.title = title;
    this.kind = kind;
    this.type = type;
    this.startDate = startDate;
    this.endDate = endDate;
    this.uuid = uuid;
    String[] infos = info.split("=-,-=");
    this.info = Arrays.stream(infos).map(Info::new).collect(Collectors.toList());
  }
}
