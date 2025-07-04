package art.heredium.domain.membership.model.dto.request;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAllActiveMembershipsRequest {
  private Long accountId;
  private LocalDateTime signUpDateFrom;
  private LocalDateTime signUpDateTo;
  private Boolean isAgreeToReceiveMarketing;
  private String text;
}
