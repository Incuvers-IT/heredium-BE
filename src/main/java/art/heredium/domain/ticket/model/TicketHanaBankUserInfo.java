package art.heredium.domain.ticket.model;

import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import art.heredium.hanabank.HanaParamsResponse;

@Getter
@Setter
@NoArgsConstructor
public class TicketHanaBankUserInfo {
  @NotEmpty private String hanaBankUuid;

  @NotEmpty private String name;

  private String email;

  private String phone;

  public TicketHanaBankUserInfo(HanaParamsResponse hanaParamsResponse) {
    this.hanaBankUuid = hanaParamsResponse.getCustNo();
    this.name = hanaParamsResponse.getCustNm();
    this.phone = hanaParamsResponse.getMbphNo();
    this.email = hanaParamsResponse.getEmalAdr();
  }
}
