package art.heredium.domain.account.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import com.querydsl.core.annotations.QueryProjection;

@Getter
@Setter
public class GetAccountTicketInviteResponse {
  private Long id;
  private String email;
  private String name;
  private String phone;
  private LocalDateTime createdDate;
  private LocalDateTime lastLoginDate;
  private Boolean isLocalResident;
  private Long visitCount;
  private Long inviteCount;

  @QueryProjection
  public GetAccountTicketInviteResponse(
      Long id,
      String email,
      String name,
      String phone,
      LocalDateTime createdDate,
      LocalDateTime lastLoginDate,
      Boolean isLocalResident,
      Long visitCount,
      Long inviteCount) {
    this.id = id;
    this.email = email;
    this.name = name;
    this.phone = phone;
    this.createdDate = createdDate;
    this.lastLoginDate = lastLoginDate;
    this.isLocalResident = isLocalResident;
    this.visitCount = visitCount;
    this.inviteCount = inviteCount;
  }
}
