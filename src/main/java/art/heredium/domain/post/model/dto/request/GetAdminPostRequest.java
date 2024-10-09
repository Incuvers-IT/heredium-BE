package art.heredium.domain.post.model.dto.request;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAdminPostRequest {
  @NotNull private SearchDateType searchDateType;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private Boolean isEnabled;
  private String name;

  @Getter
  public enum SearchDateType {
    CREATED_DATE(0, "등록일"),
    LAST_MODIFIED_DATE(1, "수정일");

    private int code;
    private String desc;

    SearchDateType(int code, String desc) {
      this.code = code;
      this.desc = desc;
    }
  }
}
