package art.heredium.domain.ticket.model.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class PostTicketNonUserCommonRequest {
  @NotEmpty private String encodeData;

  @Email private String email;

  @NotEmpty
  @Length(min = 4, max = 4)
  private String password;
}
