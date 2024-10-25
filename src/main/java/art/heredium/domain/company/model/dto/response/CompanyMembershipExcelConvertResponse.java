package art.heredium.domain.company.model.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.company.model.dto.request.CompanyMembershipRegistrationRequest;

@Getter
@Setter
public class CompanyMembershipExcelConvertResponse {
  private List<CompanyMembershipRegistrationRequest> successfulRequests;
  private List<String> failedRequests;
}
