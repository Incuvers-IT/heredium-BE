package art.heredium.domain.account.model.dto.request;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAccountTicketGroupRequest {
  @NotNull private String text;
}
