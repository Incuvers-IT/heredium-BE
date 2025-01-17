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
import art.heredium.excel.constants.UploadedMembershipRegistrationColumns;

@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class CompanyService {

  private static final Long DEFAULT_MEMBERSHIP_PERIOD = 364L; // days

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
    return companyRepository.findAllByIsDeletedFalse().stream()
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
      String identifier = this.getUniqueIdentifier(request);

      if (identifier == null) {
        companyMembershipRegistrationResponse
            .getFailedCases()
            .add(
                String.format(
                    "잘못된 요청: 이메일: %s, 핸드폰: %s, 이름: %s",
                    request.getEmail(), request.getPhone(), request.getName()));
        failedMembershipRegistrationHistoryIds.add(requestHistoryMap.get(request));
        continue;
      }

      if (request.getStartDate() == null) {
        companyMembershipRegistrationResponse
            .getFailedCases()
            .add("Invalid request: startDate is missing");
        failedMembershipRegistrationHistoryIds.add(requestHistoryMap.get(request));
        continue;
      }

      // Skip duplicate entries without adding to failed cases
      if (!processedIdentifiers.add(identifier)) {
        continue;
      }

      final Account selectedAccount =
          accountRepository
              .findLatestLoginAccountByEmailAndPhoneAndName(
                  request.getEmail(), request.getPhone(), request.getName())
              .orElse(null);

      if (selectedAccount != null && !successfulAccountIds.contains(selectedAccount.getId())) {
        // Check if the account already has an active membership
        Optional<MembershipRegistration> activeMembership =
            membershipRegistrationRepository.findCompletedOneByAccountIdAndNotExpired(
                selectedAccount.getId());

        if (activeMembership.isPresent()) {
          companyMembershipRegistrationResponse
              .getFailedCases()
              .add(
                  String.format(
                      "Account already has an active membership: %s, %s, %s",
                      request.getEmail(), request.getPhone(), request.getName()));
          failedMembershipRegistrationHistoryIds.add(requestHistoryMap.get(request));
          continue;
        }

        MembershipRegistration registration =
            createMembershipRegistration(request, selectedAccount, company);
        membershipRegistrationRepository.save(registration);

        List<Coupon> companyCoupons = couponRepository.findByCompany(company);
        couponUsageService.distributeMembershipAndCompanyCoupons(
            selectedAccount, companyCoupons, true);

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
                String.format(
                    "No account found for email:%s, phone:%s and name:%s",
                    request.getEmail(), request.getPhone(), request.getName()));
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
    if (StringUtils.isEmpty(request.getEmail())
        || StringUtils.isEmpty(request.getPhone())
        || StringUtils.isEmpty(request.getName())) {
      return null;
    }
    return String.format(
        "email:%s, phone:%s, name:%s", request.getEmail(), request.getPhone(), request.getName());
  }

  private CompanyMembershipExcelConvertResponse parseExcelFile(MultipartFile file)
      throws IOException {
    CompanyMembershipExcelConvertResponse response = new CompanyMembershipExcelConvertResponse();
    Map<CompanyMembershipRegistrationRequest, Long> successfulRequests = new HashMap<>();
    List<String> failedRequests = new ArrayList<>();
    Workbook workbook = WorkbookFactory.create(file.getInputStream());
    Sheet sheet = workbook.getSheetAt(0);

    for (Row row : sheet) {
      if (row.getRowNum() == 0) {
        this.validateUploadedMembershipRegistrationExcelColumns(row);
        continue; // Skip header row
      }
      if (isRowEmpty(row)) continue;

      final String email = getCellValueAsString(row.getCell(0));
      final String phone =
          String.format("%11s", getCellValueAsString(row.getCell(1))).replace(" ", "0");
      final String startDate = getCellValueAsString(row.getCell(2));
      final String price = getCellValueAsString(row.getCell(3));
      final String paymentDate = getCellValueAsString(row.getCell(4));
      final String name = getCellValueAsString(row.getCell(5));
      try {
        CompanyMembershipRegistrationRequest request = new CompanyMembershipRegistrationRequest();
        request.setEmail(email);
        request.setPhone(phone);
        request.setName(name);
        request.setStartDate(getCellValueAsLocalDate(row.getCell(2)));
        request.setPrice(getCellValueAsLong(row.getCell(3)));
        request.setPaymentDate(getCellValueAsLocalDate(row.getCell(4)));
        final Long registrationHistoryId =
            this.companyMembershipRegistrationHistoryService
                .createMembershipRegistrationHistory(
                    CompanyMembershipRegistrationHistoryCreateRequest.builder()
                        .email(email)
                        .phone(phone)
                        .startDate(startDate)
                        .price(price)
                        .paymentDate(paymentDate)
                        .build())
                .getId();
        successfulRequests.put(request, registrationHistoryId);
      } catch (InvalidUploadDataException e) {
        failedRequests.add(getCellValueAsString(row.getCell(0)) + ": " + e.getMessage());
        this.companyMembershipRegistrationHistoryService.createMembershipRegistrationHistory(
            CompanyMembershipRegistrationHistoryCreateRequest.builder()
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

  private void validateUploadedMembershipRegistrationExcelColumns(final @NonNull Row headerRow) {
    for (int i = 0; i <= 4; i++) {
      final String columnName = getCellValueAsString(headerRow.getCell(i));
      final String expectedColumnName =
          UploadedMembershipRegistrationColumns.getColumnNameByIndex(i);
      if (!expectedColumnName.equals(StringUtils.trim(columnName))) {
        throw new ApiException(
            ErrorCode.INVALID_EXCEL_COLUMNS,
            "Column names should be ['이메일', '핸드폰', '시작 날짜', '가격', '지불 날짜', '이름']");
      }
    }
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
          return cell.getLocalDateTimeCellValue().toLocalDate().toString();
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

    String dateStr = getCellValueAsString(cell).trim();
    // Check if the date string matches YYYY-MM-DD format
    if (!dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
      throw new InvalidUploadDataException("Date must be in YYYY-MM-DD format: " + dateStr);
    }

    try {
      return LocalDate.parse(dateStr);
    } catch (Exception e) {
      throw new InvalidUploadDataException("Invalid date: " + dateStr);
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
        account,
        company,
        request.getStartDate().atStartOfDay(),
        request.getStartDate().plusDays(DEFAULT_MEMBERSHIP_PERIOD).atTime(23, 59, 59),
        PaymentStatus.COMPLETED,
        request.getPaymentDate().atStartOfDay(),
        RegistrationType.COMPANY,
        request.getPrice());
  }

  public List<String> getExistingMembershipRegistration(MultipartFile file) {
    List<String> existingMemberships = new ArrayList<>();

    Workbook workbook;
    try {
      workbook = WorkbookFactory.create(file.getInputStream());
    } catch (IOException e) {
      throw new RuntimeException("Failed to read Excel file", e);
    }
    Sheet sheet = workbook.getSheetAt(0);

    // Skip header row and validate columns
    Row headerRow = sheet.getRow(0);
    if (headerRow != null) {
      validateUploadedMembershipRegistrationExcelColumns(headerRow);
    }

    // Process each row
    for (Row row : sheet) {
      if (row.getRowNum() == 0 || isRowEmpty(row)) continue;

      String email = getCellValueAsString(row.getCell(0));
      String phone = getCellValueAsString(row.getCell(1));

      Account selectedAccount = null;

      // Check by email
      if (StringUtils.isNotBlank(email)) {
        Optional<Account> accountByEmail = accountRepository.findLatestLoginAccountByEmail(email);
        selectedAccount = accountByEmail.orElse(null);
      }

      // Check by phone if email search failed
      if (selectedAccount == null && StringUtils.isNotBlank(phone)) {
        Optional<Account> accountByPhone = accountRepository.findLatestLoginAccountByPhone(phone);
        selectedAccount = accountByPhone.orElse(null);
      }

      // If account found, check for active membership
      if (selectedAccount != null) {
        Optional<MembershipRegistration> activeMembership =
            membershipRegistrationRepository.findCompletedOneByAccountIdAndNotExpired(
                selectedAccount.getId());

        if (activeMembership.isPresent()) {
          String identifier = email != null ? email : phone;
          MembershipRegistration membership = activeMembership.get();
          existingMemberships.add(
              String.format(
                  "Account %s has active membership until %s (Type: %s)",
                  identifier,
                  membership.getExpirationDate(),
                  membership.getMembershipOrCompanyName()));
        }
      }
    }

    try {
      workbook.close();
    } catch (IOException e) {
      throw new RuntimeException("Failed to close workbook", e);
    }
    return existingMemberships;
  }
}
