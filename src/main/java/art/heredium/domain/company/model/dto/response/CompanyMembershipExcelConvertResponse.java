package art.heredium.domain.company.model.dto.response;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.company.model.dto.request.CompanyMembershipRegistrationRequest;

@Getter
@Setter
public class CompanyMembershipExcelConvertResponse {
  private Map<CompanyMembershipRegistrationRequest, Long> successfulRequests;
  private List<String> failedRequests;
}
