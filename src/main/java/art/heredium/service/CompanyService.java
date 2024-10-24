package art.heredium.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.company.entity.Company;
import art.heredium.domain.company.model.dto.request.CompanyCreateRequest;
import art.heredium.domain.company.repository.CompanyRepository;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class CompanyService {
  private final CompanyRepository companyRepository;
  private final CouponService couponService;

  @Transactional(rollbackFor = Exception.class)
  public Long createCompany(@NonNull final CompanyCreateRequest request) {
    this.companyRepository
        .findByName(request.getName())
        .ifPresent(
            company -> {
              throw new ApiException(
                  ErrorCode.COMPANY_NAME_ALREADY_EXISTS,
                  String.format("Company name already exists: %s", company.getName()));
            });
    final Company company = Company.builder().name(request.getName()).build();
    final Company savedCompany = this.companyRepository.save(company);
    request
        .getCoupons()
        .forEach(coupon -> this.couponService.createCompanyCoupon(coupon, savedCompany));
    return savedCompany.getId();
  }
}
