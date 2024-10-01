package art.heredium.domain.common.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VerifyToken {
  private Long userid;
  private String email;
  private LocalDateTime dateTime;
}
