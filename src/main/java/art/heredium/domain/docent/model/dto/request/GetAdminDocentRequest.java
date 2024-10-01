package art.heredium.domain.docent.model.dto.request;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.common.type.DateState;
import art.heredium.domain.common.type.HallType;

@Getter
@Setter
public class GetAdminDocentRequest {
  private SearchDateType searchDateType;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private HallType hall;
  private Boolean isEnabled;
  private List<DateState> state = new ArrayList<>();
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
