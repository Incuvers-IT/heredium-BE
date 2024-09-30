package art.heredium.ncloud.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Message { // 최대 100개
    private String to, subject, content;
}
