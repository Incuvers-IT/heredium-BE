package art.heredium.domain.common.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class VerifyToken {
    private Long userid;
    private String email;
    private LocalDateTime dateTime;
}
