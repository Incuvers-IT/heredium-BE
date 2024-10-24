package art.heredium.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.ss.usermodel.*;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.repository.AccountRepository;
import art.heredium.domain.company.entity.Company;
import art.heredium.domain.company.model.dto.request.CompanyCreateRequest;
import art.heredium.domain.company.model.dto.request.CompanyMembershipRegistrationRequest;
import art.heredium.domain.company.model.dto.response.CompanyMembershipRegistrationResponse;
import art.heredium.domain.company.model.dto.response.CompanyResponseDto;
import art.heredium.domain.company.repository.CompanyRepository;
import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.repository.CouponRepository;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.entity.PaymentStatus;
import art.heredium.domain.membership.entity.RegistrationType;
import art.heredium.domain.membership.repository.MembershipRegistrationRepository;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class CompanyService {
  private final CompanyRepository companyRepository;
  private final CouponRepository couponRepository;
  private final AccountRepository accountRepository;
  private final MembershipRegistrationRepository membershipRegistrationRepository;
  private final CouponUsageService couponUsageService;
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

  public List<CompanyResponseDto> getAllCompanies() {
    return companyRepository.findAll().stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  private CompanyResponseDto convertToDto(Company company) {
    CompanyResponseDto dto = new CompanyResponseDto();
    dto.setId(company.getId());
    dto.setName(company.getName());
    return dto;
  }

  @Transactional(rollbackFor = Exception.class)
  public CompanyMembershipRegistrationResponse uploadMembershipRegistration(
      Long companyId, MultipartFile file) throws IOException {
    Company company =
        companyRepository
            .findById(companyId)
            .orElseThrow(() -> new ApiException(ErrorCode.COMPANY_NOT_FOUND));

    List<CompanyMembershipRegistrationRequest> requests = parseExcelFile(file);
    CompanyMembershipRegistrationResponse response = new CompanyMembershipRegistrationResponse();
    response.setSuccessCases(new ArrayList<>());
    response.setFailedCases(new ArrayList<>());

    for (CompanyMembershipRegistrationRequest request : requests) {
      Optional<Account> accountOpt =
          accountRepository.findByEmailOrAccountInfo_Phone(request.getEmailOrPhone());

      if (accountOpt.isPresent()) {
        Account account = accountOpt.get();
        MembershipRegistration registration =
            createMembershipRegistration(request, account, company);
        membershipRegistrationRepository.save(registration);

        List<Coupon> companyCoupons = couponRepository.findByCompany(company);
        couponUsageService.distributeMembershipAndCompanyCoupons(account, companyCoupons);

        response.getSuccessCases().add(request.getEmailOrPhone());
      } else {
        response.getFailedCases().add(request.getEmailOrPhone());
      }
    }

    return response;
  }

  private List<CompanyMembershipRegistrationRequest> parseExcelFile(MultipartFile file)
      throws IOException {
    List<CompanyMembershipRegistrationRequest> requests = new ArrayList<>();
    Workbook workbook = WorkbookFactory.create(file.getInputStream());
    Sheet sheet = workbook.getSheetAt(0);

    for (Row row : sheet) {
      if (row.getRowNum() == 0) continue; // Skip header row

      // Check if the row is empty
      if (isRowEmpty(row)) continue;

      CompanyMembershipRegistrationRequest request = new CompanyMembershipRegistrationRequest();
      request.setTitle(getCellValueAsString(row.getCell(0)));
      request.setEmailOrPhone(getCellValueAsString(row.getCell(1)));
      request.setStartDate(getCellValueAsLocalDate(row.getCell(2)));
      request.setPrice(getCellValueAsInteger(row.getCell(3)));
      request.setPaymentDate(getCellValueAsLocalDate(row.getCell(4)));

      requests.add(request);
    }

    workbook.close();
    return requests;
  }

  private boolean isRowEmpty(Row row) {
    for (Cell cell : row) {
      if (cell != null && cell.getCellType() != CellType.BLANK) {
        return false;
      }
    }
    return true;
  }

  private String getCellValueAsString(Cell cell) {
    if (cell == null) return null;
    switch (cell.getCellType()) {
      case STRING:
        return cell.getStringCellValue();
      case NUMERIC:
        if (DateUtil.isCellDateFormatted(cell)) {
          return cell.getLocalDateTimeCellValue().toString();
        }
        // Use BigDecimal to avoid scientific notation
        return new BigDecimal(cell.getNumericCellValue()).toPlainString();
      case BOOLEAN:
        return String.valueOf(cell.getBooleanCellValue());
      case FORMULA:
        return cell.getCellFormula();
      default:
        return "";
    }
  }

  private LocalDate getCellValueAsLocalDate(Cell cell) {
    if (cell == null) return null;
    return cell.getLocalDateTimeCellValue().toLocalDate();
  }

  private Integer getCellValueAsInteger(Cell cell) {
    if (cell == null) return null;
    return (int) cell.getNumericCellValue();
  }

  private MembershipRegistration createMembershipRegistration(
      CompanyMembershipRegistrationRequest request, Account account, Company company) {
    return new MembershipRegistration(
        request.getTitle(),
        account,
        company,
        request.getStartDate(),
        request.getStartDate().plusDays(364),
        PaymentStatus.COMPLETED,
        request.getPaymentDate(),
        RegistrationType.COMPANY,
        request.getPrice());
  }
}
