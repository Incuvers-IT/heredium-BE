package art.heredium.domain.company.model.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyUpdateRequest {
  private String name;

  @JsonProperty("is_deleted")
  private Boolean isDeleted;

  private List<CompanyCouponUpdateRequest> coupons;
}
