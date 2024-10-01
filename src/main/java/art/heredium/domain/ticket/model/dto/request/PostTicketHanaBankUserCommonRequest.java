package art.heredium.domain.ticket.model.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.validator.constraints.Length;

import art.heredium.hanabank.HanaParamsRequest;

@Getter
@Setter
public class PostTicketHanaBankUserCommonRequest extends HanaParamsRequest {

  @Email private String email;

  @NotEmpty private String phone;

  @NotEmpty
  @Length(min = 4, max = 4)
  private String password;
}
