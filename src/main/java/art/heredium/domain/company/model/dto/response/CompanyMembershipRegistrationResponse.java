package art.heredium.domain.company.model.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class CompanyMembershipRegistrationResponse {
  @JsonProperty("success_cases")
  private List<String> successCases;

  @JsonProperty("failed_cases")
  private List<String> failedCases;
}
