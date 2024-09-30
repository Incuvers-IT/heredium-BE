package art.heredium.domain.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class VerifyLoginCode {
    private String code;
    private LocalDateTime dateTime;
}
