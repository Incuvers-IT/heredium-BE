package art.heredium.domain.company.model.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

import art.heredium.domain.company.entity.Company;
import art.heredium.domain.coupon.entity.Coupon;

@Data
public class CompanyDetailResponse {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("name")
  private String name;

  @JsonProperty("coupons")
  private List<CompanyCouponResponse> coupons;

  public CompanyDetailResponse(Company company, List<Coupon> coupons) {
    this.id = company.getId();
    this.name = company.getName();
    this.coupons = coupons.stream().map(CompanyCouponResponse::new).collect(Collectors.toList());
  }
}
