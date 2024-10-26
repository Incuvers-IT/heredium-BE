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

import org.apache.commons.lang3.StringUtils;
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
import art.heredium.domain.membership.entity.RegistrationStatus;
import art.heredium.domain.membership.entity.RegistrationType;
import art.heredium.domain.membership.model.dto.request.CompanyMembershipRegistrationHistoryCreateRequest;
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
  private final CompanyMembershipRegistrationHistoryService
      companyMembershipRegistrationHistoryService;

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
    Map<CompanyMembershipRegistrationRequest, Long> requestHistoryMap =
        response.getSuccessfulRequests();
    List<CompanyMembershipRegistrationRequest> requests =
        new ArrayList<>(requestHistoryMap.keySet());
    List<Long> successMembershipRegistrationHistoryIds = new ArrayList<>();
    List<Long> failedMembershipRegistrationHistoryIds = new ArrayList<>();
    List<String> initialFailedCases = response.getFailedRequests();

    CompanyMembershipRegistrationResponse companyMembershipRegistrationResponse =
        new CompanyMembershipRegistrationResponse();
    companyMembershipRegistrationResponse.setSuccessCases(new ArrayList<>());
    companyMembershipRegistrationResponse.setFailedCases(new ArrayList<>(initialFailedCases));

    Set<String> processedIdentifiers = new HashSet<>();
    List<Long> successfulAccountIds = new ArrayList<>(); // Changed to List

    for (CompanyMembershipRegistrationRequest request : requests) {
      String identifier = getUniqueIdentifier(request);

      if (identifier == null) {
        companyMembershipRegistrationResponse
            .getFailedCases()
            .add("Invalid request: both email and phone are missing");
        failedMembershipRegistrationHistoryIds.add(requestHistoryMap.get(request));
        continue;
      }

      // Skip duplicate entries without adding to failed cases
      if (!processedIdentifiers.add(identifier)) {
        continue;
      }

      Account selectedAccount = null;

      // First, try to find an account by email with the latest login
      if (StringUtils.isNotBlank(request.getEmail())) {
        Optional<Account> accountByEmail =
            accountRepository.findLatestLoginAccountByEmail(request.getEmail());
        selectedAccount = accountByEmail.orElse(null);
      }

      // If no account found by email, search by phone
      if (selectedAccount == null && request.getPhone() != null) {
        Optional<Account> accountByPhone =
            accountRepository.findLatestLoginAccountByPhone(request.getPhone());
        selectedAccount = accountByPhone.orElse(null);
      }

      if (selectedAccount != null && !successfulAccountIds.contains(selectedAccount.getId())) {
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
          failedMembershipRegistrationHistoryIds.add(requestHistoryMap.get(request));
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

        successfulAccountIds.add(
            selectedAccount.getId()); // Add the account ID to the successful list
        successMembershipRegistrationHistoryIds.add(requestHistoryMap.get(request));
      } else {
        companyMembershipRegistrationResponse
            .getFailedCases()
            .add(
                "No account found for email: "
                    + request.getEmail()
                    + " or phone: "
                    + request.getPhone());
        failedMembershipRegistrationHistoryIds.add(requestHistoryMap.get(request));
      }
    }
    this.updateCompanyMembershipRegistrationStatus(
        successMembershipRegistrationHistoryIds, RegistrationStatus.SUCCESS);
    this.updateCompanyMembershipRegistrationStatus(
        failedMembershipRegistrationHistoryIds, RegistrationStatus.FAILED);

    return companyMembershipRegistrationResponse;
  }

  private void updateCompanyMembershipRegistrationStatus(
      final List<Long> membershipRegistrationHistoryIds, final RegistrationStatus status) {
    this.companyMembershipRegistrationHistoryService.updateRegistrationStatus(
        membershipRegistrationHistoryIds, status);
  }

  private String getUniqueIdentifier(CompanyMembershipRegistrationRequest request) {
    if (request.getEmail() != null) {
      return "email:" + request.getEmail();
    } else if (request.getPhone() != null) {
      return "phone:" + request.getPhone();
    }
    return null;
  }

  private CompanyMembershipExcelConvertResponse parseExcelFile(MultipartFile file)
      throws IOException {
    CompanyMembershipExcelConvertResponse response = new CompanyMembershipExcelConvertResponse();
    Map<CompanyMembershipRegistrationRequest, Long> successfulRequests = new HashMap<>();
    List<String> failedRequests = new ArrayList<>();
    Workbook workbook = WorkbookFactory.create(file.getInputStream());
    Sheet sheet = workbook.getSheetAt(0);

    for (Row row : sheet) {
      if (row.getRowNum() == 0) continue; // Skip header row
      if (isRowEmpty(row)) continue;

      final String title = getCellValueAsString(row.getCell(0));
      final String email = getCellValueAsString(row.getCell(1));
      final String phone = getCellValueAsString(row.getCell(2));
      final String startDate = getCellValueAsString(row.getCell(3));
      final String price = getCellValueAsString(row.getCell(4));
      final String paymentDate = getCellValueAsString(row.getCell(5));
      try {
        CompanyMembershipRegistrationRequest request = new CompanyMembershipRegistrationRequest();
        request.setTitle(title);
        request.setEmail(email);
        request.setPhone(phone);
        request.setStartDate(getCellValueAsLocalDate(row.getCell(3)));
        request.setPrice(getCellValueAsLong(row.getCell(4)));
        request.setPaymentDate(getCellValueAsLocalDate(row.getCell(5)));
        final Long registrationHistoryId =
            this.companyMembershipRegistrationHistoryService
                .createMembershipRegistrationHistory(
                    CompanyMembershipRegistrationHistoryCreateRequest.builder()
                        .title(title)
                        .email(email)
                        .phone(phone)
                        .startDate(startDate)
                        .price(price)
                        .paymentDate(paymentDate)
                        .build())
                .getId();
        successfulRequests.put(request, registrationHistoryId);
      } catch (InvalidUploadDataException e) {
        failedRequests.add(getCellValueAsString(row.getCell(1)) + ": " + e.getMessage());
        this.companyMembershipRegistrationHistoryService.createMembershipRegistrationHistory(
            CompanyMembershipRegistrationHistoryCreateRequest.builder()
                .title(title)
                .email(email)
                .phone(phone)
                .startDate(startDate)
                .price(price)
                .paymentDate(paymentDate)
                .status(RegistrationStatus.FAILED)
                .failedReason(e.getMessage())
                .build());
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
      throw new InvalidUploadDataException("Invalid date: " + getCellValueAsString(cell));
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
