package art.heredium.domain.account.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import com.querydsl.core.annotations.QueryProjection;

@Getter
@Setter
public class GetAdminSleeperResponse {
  private Long id;
  private String email;
  private String name;
  private String phone;
  private LocalDateTime createdDate;
  private LocalDateTime sleepDate;
  private Boolean isMarketingReceive;
  private Long visitCount;

  @QueryProjection
  public GetAdminSleeperResponse(
      Long id,
      String email,
      String name,
      String phone,
      LocalDateTime createdDate,
      LocalDateTime sleepDate,
      Boolean isMarketingReceive,
      Long visitCount) {
    this.id = id;
    this.email = email;
    this.name = name;
    this.phone = phone;
    this.createdDate = createdDate;
    this.sleepDate = sleepDate;
    this.isMarketingReceive = isMarketingReceive;
    this.visitCount = visitCount;
  }
}
