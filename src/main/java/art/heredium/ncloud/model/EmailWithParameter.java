package art.heredium.ncloud.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class EmailWithParameter {
    private String email;
    private Map<String, String> parameters = null;

    public EmailWithParameter() {

    }

    public EmailWithParameter(String x) {
        this.email = x;
    }

    public EmailWithParameter(String x, Map<String, String> parameters) {
        this.email = x;
        this.parameters = parameters;
    }
}
