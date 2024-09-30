package art.heredium.domain.account.model.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class GetUserNonUserTicketRequest {
    @NotEmpty
    private String name;

    @NotEmpty
    private String phone;

    @NotEmpty
    @Length(min = 4, max = 4)
    private String password;
}
