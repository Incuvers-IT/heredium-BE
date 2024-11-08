package art.heredium.domain.account.model.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UploadCouponIssuanceTemplateResponse {

  @JsonProperty("success_cases")
  private List<CouponIssuanceUploadResponse> successCases;

  @JsonProperty("failed_cases")
  private List<String> failedCases;
}
