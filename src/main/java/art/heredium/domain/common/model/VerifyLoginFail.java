package art.heredium.domain.common.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class VerifyLoginFail {
  private Integer count;
  private LocalDateTime dateTime;
}
