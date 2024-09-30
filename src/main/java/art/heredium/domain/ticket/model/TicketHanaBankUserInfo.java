package art.heredium.domain.ticket.model;

import art.heredium.hanabank.HanaParamsResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
public class TicketHanaBankUserInfo {
    @NotEmpty
    private String hanaBankUuid;

    @NotEmpty
    private String name;

    private String email;

    private String phone;

    public TicketHanaBankUserInfo(HanaParamsResponse hanaParamsResponse) {
        this.hanaBankUuid = hanaParamsResponse.getCustNo();
        this.name = hanaParamsResponse.getCustNm();
        this.phone = hanaParamsResponse.getMbphNo();
        this.email = hanaParamsResponse.getEmalAdr();
    }
}