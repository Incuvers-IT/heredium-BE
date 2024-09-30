package art.heredium.ncloud.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RecipientForRequest {
    private String address = null;
    private String name = null;
    private String type = "R";
    private Object parameters = null;
}
