package art.heredium.domain.company.model.dto.request;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.coupon.model.dto.request.CompanyCouponCreateRequest;

@Getter
@Setter
public class CompanyCreateRequest {
  @NotBlank private String name;

  @NotEmpty @Valid private List<CompanyCouponCreateRequest> coupons;
}
