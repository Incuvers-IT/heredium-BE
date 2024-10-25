package art.heredium.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.apache.poi.ss.usermodel.*;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.config.error.entity.InvalidUploadDataException;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.repository.AccountRepository;
import art.heredium.domain.company.entity.Company;
import art.heredium.domain.company.model.dto.request.CompanyCreateRequest;
import art.heredium.domain.company.model.dto.request.CompanyMembershipRegistrationRequest;
import art.heredium.domain.company.model.dto.response.CompanyMembershipExcelConvertResponse;
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

    CompanyMembershipExcelConvertResponse response = parseExcelFile(file);
    List<CompanyMembershipRegistrationRequest> requests = response.getSuccessfulRequests();
    List<String> initialFailedCases = response.getFailedRequests();

    CompanyMembershipRegistrationResponse companyMembershipRegistrationResponse =
        new CompanyMembershipRegistrationResponse();
    companyMembershipRegistrationResponse.setSuccessCases(new ArrayList<>());
    companyMembershipRegistrationResponse.setFailedCases(new ArrayList<>(initialFailedCases));

    // Set to keep track of processed emails and phones
    Set<String> processedEmails = new HashSet<>();
    Set<String> processedPhones = new HashSet<>();

    for (CompanyMembershipRegistrationRequest request : requests) {
      if (request.getEmail() == null && request.getPhone() == null) {
        companyMembershipRegistrationResponse
            .getFailedCases()
            .add("Invalid request: both email and phone are missing");
        continue;
      }

      // Check for duplicate email or phone
      if ((request.getEmail() != null && !processedEmails.add(request.getEmail()))
          || (request.getPhone() != null && !processedPhones.add(request.getPhone()))) {
        companyMembershipRegistrationResponse
            .getFailedCases()
            .add(
                "Duplicate entry: "
                    + (request.getEmail() != null ? request.getEmail() : request.getPhone()));
        continue;
      }

      Account selectedAccount = null;

      // First, try to find an account by email
      if (request.getEmail() != null) {
        Optional<Account> accountByEmail = accountRepository.findByEmail(request.getEmail());
        selectedAccount = accountByEmail.orElse(null);
      }

      // If no account found by email, search by phone
      if (selectedAccount == null && request.getPhone() != null) {
        Optional<Account> accountByPhone =
            accountRepository.findLatestLoginAccountByPhone(request.getPhone());
        selectedAccount = accountByPhone.orElse(null);
      }

      if (selectedAccount != null) {
        // Check if the account already has an active membership
        Optional<MembershipRegistration> activeMembership =
            membershipRegistrationRepository.findByAccountAndExpirationDateAfter(
                selectedAccount, LocalDate.now());

        if (activeMembership.isPresent()) {
          companyMembershipRegistrationResponse
              .getFailedCases()
              .add(
                  "Account already has an active membership: "
                      + (request.getEmail() != null ? request.getEmail() : request.getPhone()));
          continue;
        }

        MembershipRegistration registration =
            createMembershipRegistration(request, selectedAccount, company);
        membershipRegistrationRepository.save(registration);

        List<Coupon> companyCoupons = couponRepository.findByCompany(company);
        couponUsageService.distributeMembershipAndCompanyCoupons(selectedAccount, companyCoupons);

        companyMembershipRegistrationResponse
            .getSuccessCases()
            .add(request.getEmail() != null ? request.getEmail() : request.getPhone());
      } else {
        companyMembershipRegistrationResponse
            .getFailedCases()
            .add(
                "No account found for email: "
                    + request.getEmail()
                    + " or phone: "
                    + request.getPhone());
      }
    }

    return companyMembershipRegistrationResponse;
  }

  private CompanyMembershipExcelConvertResponse parseExcelFile(MultipartFile file)
      throws IOException {
    CompanyMembershipExcelConvertResponse response = new CompanyMembershipExcelConvertResponse();
    List<CompanyMembershipRegistrationRequest> successfulRequests = new ArrayList<>();
    List<String> failedRequests = new ArrayList<>();
    Workbook workbook = WorkbookFactory.create(file.getInputStream());
    Sheet sheet = workbook.getSheetAt(0);

    for (Row row : sheet) {
      if (row.getRowNum() == 0) continue; // Skip header row
      if (isRowEmpty(row)) continue;

      try {
        CompanyMembershipRegistrationRequest request = new CompanyMembershipRegistrationRequest();
        request.setTitle(getCellValueAsString(row.getCell(0)));
        request.setEmail(getCellValueAsString(row.getCell(1)));
        request.setPhone(getCellValueAsString(row.getCell(2)));
        request.setStartDate(getCellValueAsLocalDate(row.getCell(3)));
        request.setPrice(getCellValueAsLong(row.getCell(4)));
        request.setPaymentDate(getCellValueAsLocalDate(row.getCell(5)));
        successfulRequests.add(request);
      } catch (InvalidUploadDataException e) {
        failedRequests.add(getCellValueAsString(row.getCell(1)) + ": " + e.getMessage());
      }
    }

    workbook.close();
    response.setSuccessfulRequests(successfulRequests);
    response.setFailedRequests(failedRequests);
    return response;
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
    try {
      return cell.getLocalDateTimeCellValue().toLocalDate();
    } catch (Exception e) {
      throw new InvalidUploadDataException("Invalide date: " + getCellValueAsString(cell));
    }
  }

  private Long getCellValueAsLong(Cell cell) {
    if (cell == null) return null;
    try {
      return Long.parseLong(getCellValueAsString(cell));
    } catch (Exception e) {
      throw new InvalidUploadDataException("Invalid number: " + getCellValueAsString(cell));
    }
  }

  private MembershipRegistration createMembershipRegistration(
      CompanyMembershipRegistrationRequest request, Account account, Company company) {
    return new MembershipRegistration(
        request.getTitle(),
        account,
        company,
        request.getStartDate(),
        request.getStartDate().plusDays(365),
        PaymentStatus.COMPLETED,
        request.getPaymentDate(),
        RegistrationType.COMPANY,
        request.getPrice());
  }
}
