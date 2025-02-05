package art.heredium.domain.company.model.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyUpdateRequest {
  private String name;

  private Boolean isDeleted;

  private List<CompanyCouponUpdateRequest> coupons;
}
