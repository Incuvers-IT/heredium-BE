package art.heredium.domain.account.model.dto.request;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAdminAccountRequest {
  @NotNull private SearchDateType searchDateType;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private Boolean isMarketingReceive;
  private String text;

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
