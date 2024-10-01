package art.heredium.domain.account.model.dto.request;

import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostLoginRequest {
  @NotEmpty(message = "아이디를 입력해주세요.")
  private String email;

  @NotEmpty(message = "비밀번호를 입력해주세요.")
  private String password;
}
