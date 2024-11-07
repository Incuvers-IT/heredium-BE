package art.heredium.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.config.properties.AppProperties;
import art.heredium.core.config.properties.HerediumProperties;
import art.heredium.core.jwt.AuthToken;
import art.heredium.core.jwt.JwtRedisUtil;
import art.heredium.core.jwt.MailTokenProvider;
import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.account.model.dto.request.*;
import art.heredium.domain.account.model.dto.response.*;
import art.heredium.domain.account.repository.AccountInfoRepository;
import art.heredium.domain.account.repository.AccountRepository;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.repository.MembershipRegistrationRepository;
import art.heredium.domain.ticket.repository.TicketRepository;
import art.heredium.domain.ticket.type.TicketKindType;
import art.heredium.excel.constants.CouponIssuanceTemplateColumns;
import art.heredium.ncloud.bean.CloudMail;
import art.heredium.ncloud.bean.HerediumAlimTalk;
import art.heredium.ncloud.type.AlimTalkTemplate;
import art.heredium.ncloud.type.MailTemplate;
import art.heredium.niceId.model.dto.response.PostNiceIdEncryptResponse;
import art.heredium.niceId.service.NiceIdService;
import art.heredium.oauth.provider.OAuth2Provider;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class AccountService {

  private final AuthService authService;
  private final NiceIdService niceIdService;
  private final AccountRepository accountRepository;
  private final AccountInfoRepository accountInfoRepository;
  private final TicketRepository ticketRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final AppProperties appProperties;
  private final CloudMail cloudMail;
  private final MailTokenProvider mailTokenProvider;
  private final JwtRedisUtil jwtRedisUtil;
  private final HerediumAlimTalk alimTalk;
  private final HerediumProperties herediumProperties;
  private final MembershipRegistrationRepository membershipRegistrationRepository;

  public GetUserAccountResponse get(String password) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    if (userPrincipal.getProvider() == OAuth2Provider.EMAIL
        && !bCryptPasswordEncoder.matches(password, userPrincipal.getPassword())) {
      throw new ApiException(ErrorCode.PASSWORD_NOT_MATCHED);
    }
    return new GetUserAccountResponse(userPrincipal.getAccount());
  }

  public GetUserAccountInfoResponse info() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    final Account account = userPrincipal.getAccount();
    final MembershipRegistration membershipRegistration =
        membershipRegistrationRepository.findLatestForAccount(account.getId()).orElse(null);
    return new GetUserAccountInfoResponse(userPrincipal.getAccount(), membershipRegistration);
  }

  public PostLoginResponse insert(HttpServletResponse response, PostAccountRequest dto) {
    if (isExistEmail(dto.getEmail())) {
      throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
    }
    PostNiceIdEncryptResponse info = niceIdService.decrypt(dto.getEncodeData());
    long age = ChronoUnit.YEARS.between(info.getBirthDate(), Constants.getNow());
    if (age < 14) {
      throw new ApiException(ErrorCode.UNDER_FOURTEEN);
    }
    Account entity = new Account(dto, info, bCryptPasswordEncoder.encode(dto.getPassword()));
    accountRepository.saveAndFlush(entity);

    PostLoginResponse res =
        authService.login(response, new PostLoginRequest(dto.getEmail(), dto.getPassword()), false);

    Map<String, String> params = new HashMap<>();
    params.put("name", entity.getAccountInfo().getName());
    params.put("link", herediumProperties.getDomain());
    params.put("CSTel", herediumProperties.getTel());
    params.put("CSEmail", herediumProperties.getEmail());
    cloudMail.mail(entity.getEmail(), params, MailTemplate.SIGN_UP);
    alimTalk.sendAlimTalk(entity.getAccountInfo().getPhone(), params, AlimTalkTemplate.SIGN_UP);
    return res;
  }

  public boolean isExistEmail(String email) {
    return accountRepository.existsAccountByEmailAndProviderType(email, OAuth2Provider.EMAIL);
  }

  public boolean update(Long id, PutAdminAccountRequest dto) {
    Account entity = accountRepository.findById(id).orElse(null);
    if (entity == null || (entity.getAccountInfo() == null && entity.getSleeperInfo() == null)) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    if (entity.getProviderType() == OAuth2Provider.EMAIL
        && !entity.getEmail().equals(dto.getEmail())
        && isExistEmail(dto.getEmail())) {
      throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
    }

    entity.update(dto);
    if (entity.getAccountInfo() != null) {
      entity.getAccountInfo().update(dto);
    }
    if (entity.getSleeperInfo() != null) {
      entity.getSleeperInfo().update(dto);
    }
    return true;
  }

  public Page<GetAccountTicketGroupResponse> ticketGroup(
      GetAccountTicketGroupRequest dto, Pageable pageable) {
    return accountRepository.search(dto, pageable);
  }

  public Page<GetAccountTicketInviteResponse> ticketInvite(
      GetAccountTicketInviteRequest dto, Pageable pageable) {
    return accountRepository.search(dto, pageable);
  }

  public Page<GetAdminAccountResponse> list(GetAdminAccountRequest dto, Pageable pageable) {
    return accountRepository.search(dto, pageable);
  }

  public GetAdminAccountDetailResponse detailByAdmin(Long id) {
    Account entity = accountRepository.findById(id).orElse(null);
    if (entity == null || (entity.getAccountInfo() == null && entity.getSleeperInfo() == null)) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    return new GetAdminAccountDetailResponse(entity);
  }

  public Page<GetAdminAccountTicketResponse> ticketByAccount(
      Long id, Boolean isCoffee, Pageable pageable) {
    List<TicketKindType> ticketKindTypes =
        Arrays.stream(TicketKindType.values())
            .filter(kind -> isCoffee == (kind == TicketKindType.COFFEE))
            .collect(Collectors.toList());
    return ticketRepository
        .findAllByAccount_IdAndKindInOrderByCreatedDateDesc(id, ticketKindTypes, pageable)
        .map(GetAdminAccountTicketResponse::new);
  }

  public boolean password(Long id, PutAccountPasswordRequest dto) {
    Account entity = accountRepository.findById(id).orElse(null);
    if (entity == null || entity.getProviderType() != OAuth2Provider.EMAIL) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    entity.updatePassword(dto.getPassword(), bCryptPasswordEncoder.encode(dto.getPassword()));
    return true;
  }

  public Page<GetAdminSleeperResponse> sleepers(GetAdminSleeperRequest dto, Pageable pageable) {
    return accountRepository.search(dto, pageable);
  }

  public List<GetAuthFindIdResponse> findId(String encodeData) {
    PostNiceIdEncryptResponse info = niceIdService.decrypt(encodeData);
    List<Account> accounts = accountRepository.findEmailByPhone(info.getMobileNo());
    return accounts.stream().map(GetAuthFindIdResponse::new).collect(Collectors.toList());
  }

  public String findPwByPhone(String email, String encodeData) {
    PostNiceIdEncryptResponse info = niceIdService.decrypt(encodeData);
    boolean isExist =
        accountRepository.existsByEmailAndAccountInfo_PhoneAndProviderType(
            email, info.getMobileNo(), OAuth2Provider.EMAIL);
    if (!isExist) {
      throw new ApiException(ErrorCode.USER_NOT_FOUND);
    }
    AuthToken authToken =
        mailTokenProvider.createAuthToken(
            email,
            new Date(
                new Date().getTime() + appProperties.getAuth().getMailTokenExpiry().toMillis()));
    jwtRedisUtil.setDataExpire(
        authToken.getToken(), email, appProperties.getAuth().getMailTokenExpiry().getSeconds());
    return authToken.getToken();
  }

  public boolean findPwByEmail(String email, String redirectUrl) {
    boolean isExist =
        accountRepository.existsAccountByEmailAndProviderType(email, OAuth2Provider.EMAIL);
    if (!isExist) {
      throw new ApiException(ErrorCode.USER_NOT_FOUND);
    }
    AuthToken authToken =
        mailTokenProvider.createAuthToken(
            email,
            new Date(
                new Date().getTime() + appProperties.getAuth().getMailTokenExpiry().toMillis()));
    redirectUrl += ("?token=" + authToken.getToken());
    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Map<String, String> params = new HashMap<>();
    params.put("link", redirectUrl);
    params.put("expired_date", formatter.format(authToken.getExpiration()));
    cloudMail.mail(email, params, MailTemplate.PASSWORD_CHANGE_USER);
    jwtRedisUtil.setDataExpire(
        authToken.getToken(), email, appProperties.getAuth().getMailTokenExpiry().getSeconds());
    return true;
  }

  public boolean changePw(PostAuthFindPwRequest dto) {
    AuthToken authToken = mailTokenProvider.convertAuthToken(dto.getToken());
    if (!authToken.validate() || authToken.isExpired()) {
      throw new ApiException(ErrorCode.BAD_VALID);
    }
    String email = authToken.getTokenClaims().getSubject();
    if (jwtRedisUtil.getData(authToken.getToken()) == null) {
      throw new ApiException(ErrorCode.BAD_VALID);
    }
    Account account =
        accountRepository.findByEmailEqualsAndProviderType(email, OAuth2Provider.EMAIL);
    if (account == null || (account.getAccountInfo() == null && account.getSleeperInfo() == null)) {
      throw new ApiException(ErrorCode.DATA_NOT_FOUND);
    }
    account.updatePassword(dto.getPassword(), bCryptPasswordEncoder.encode(dto.getPassword()));
    jwtRedisUtil.deleteData(authToken.getToken());
    jwtRedisUtil.deleteData(email + false); // 로그인 실패 카운트 초기화
    return true;
  }

  public GetUserAccountInfoResponse updateByAccount(PutUserAccountRequest dto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    Account entity = accountRepository.findById(userPrincipal.getId()).orElse(null);
    MembershipRegistration membershipRegistration =
        membershipRegistrationRepository.findLatestForAccount(entity.getId()).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.NOT_FOUND);
    }

    if (entity.getProviderType() == OAuth2Provider.EMAIL) {
      if (dto.getEmail() != null) {
        if (!entity.getEmail().equals(dto.getEmail()) && isExistEmail(dto.getEmail())) {
          throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
        }
        entity.updateEmail(dto.getEmail());
      }
      if (dto.getPassword() != null) {
        entity.updatePassword(dto.getPassword(), bCryptPasswordEncoder.encode(dto.getPassword()));
      }
    }
    entity.getAccountInfo().update(dto);

    if (dto.getEncodeData() != null) {
      PostNiceIdEncryptResponse info = niceIdService.decrypt(dto.getEncodeData());
      entity.getAccountInfo().updatePhone(info);
    }
    return new GetUserAccountInfoResponse(entity, membershipRegistration);
  }

  public boolean delete(@Valid @NotBlank String password) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    if (userPrincipal.getProvider() == OAuth2Provider.EMAIL) {
      boolean isMatch = bCryptPasswordEncoder.matches(password, userPrincipal.getPassword());
      if (!isMatch) {
        throw new ApiException(ErrorCode.PASSWORD_NOT_MATCHED);
      }
    }
    Account entity = accountRepository.findById(userPrincipal.getId()).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.NOT_FOUND);
    }
    Map<String, String> params = new HashMap<>();
    params.put("name", entity.getAccountInfo().getName());
    params.put("CSTel", herediumProperties.getTel());
    params.put("CSEmail", herediumProperties.getEmail());

    String email = entity.getEmail();
    String phone = entity.getAccountInfo().getPhone();
    entity.terminate();
    ticketRepository.terminateByAccount(entity.getId());

    cloudMail.mail(email, params, MailTemplate.ACCOUNT_TERMINATE);
    alimTalk.sendAlimTalk(phone, params, AlimTalkTemplate.ACCOUNT_TERMINATE);
    return true;
  }

  public boolean updateLocalResident(Boolean isEnabled) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    Account entity = accountRepository.findById(userPrincipal.getId()).orElse(null);
    if (entity == null) {
      throw new ApiException(ErrorCode.NOT_FOUND);
    }
    if (entity.getAccountInfo() != null) {
      entity.getAccountInfo().updateLocalResident(isEnabled);
    }
    return true;
  }

  public Page<AccountWithMembershipInfoResponse> listWithMembershipInfo(
      GetAccountWithMembershipInfoRequest dto, Pageable pageable) {
    return accountRepository.searchWithMembershipInfo(dto, pageable);
  }

  public Page<AccountWithMembershipInfoIncludingTitleResponse> listWithMembershipInfoIncludingTitle(
      GetAccountWithMembershipInfoIncludingTitleRequest dto, Pageable pageable) {
    return accountRepository.searchWithMembershipInfoIncludingTitle(dto, pageable);
  }

  @Transactional(rollbackFor = Exception.class)
  public List<AccountWithMembershipInfoResponse> uploadCouponIssuance(MultipartFile file)
      throws IOException {
    List<AccountWithMembershipInfoResponse> accounts = new ArrayList<>();
    Set<String> processedIdentifiers = new HashSet<>();

    Workbook workbook = WorkbookFactory.create(file.getInputStream());
    Sheet sheet = workbook.getSheetAt(0);

    // Skip header row and process each record
    for (Row row : sheet) {
      if (row.getRowNum() == 0) {
        validateHeaderRow(row);
        continue;
      }
      if (isRowEmpty(row)) continue;

      String email = getCellValueAsString(row.getCell(0));
      String phone = getCellValueAsString(row.getCell(1));

      // Only check email for duplicates initially
      boolean isDuplicateEmail =
          StringUtils.isNotBlank(email) && processedIdentifiers.contains(email);
      if (isDuplicateEmail) {
        continue;
      }

      // Always try email first if present
      if (StringUtils.isNotBlank(email)) {
        Optional<Account> accountByEmail = accountRepository.findLatestLoginAccountByEmail(email);
        if (accountByEmail.isPresent()) {
          Account selectedAccount = accountByEmail.get();
          processedIdentifiers.add(email);
          String associatedPhone = selectedAccount.getAccountInfo().getPhone();
          if (StringUtils.isNotBlank(associatedPhone)) {
            processedIdentifiers.add(associatedPhone);
          }
          addAccountToList(selectedAccount, accounts);
          continue;
        }
      }

      // If no account found by email, try phone if not processed
      if (StringUtils.isNotBlank(phone) && !processedIdentifiers.contains(phone)) {
        Optional<Account> accountByPhone = accountRepository.findLatestLoginAccountByPhone(phone);
        if (accountByPhone.isPresent()) {
          Account selectedAccount = accountByPhone.get();
          processedIdentifiers.add(phone);
          String associatedEmail = selectedAccount.getEmail();
          if (StringUtils.isNotBlank(associatedEmail)) {
            processedIdentifiers.add(associatedEmail);
          }
          addAccountToList(selectedAccount, accounts);
        }
      }
    }

    workbook.close();
    return accounts;
  }

  private void addAccountToList(Account account, List<AccountWithMembershipInfoResponse> accounts) {
    AccountWithMembershipInfoResponse accountInfo =
        accountRepository.findAccountWithMembershipInfo(account);
    accounts.add(accountInfo);
  }

  private void validateHeaderRow(Row headerRow) {
    for (int i = 0; i <= 2; i++) {
      final String columnName = getCellValueAsString(headerRow.getCell(i));
      final String expectedColumnName = CouponIssuanceTemplateColumns.getColumnNameByIndex(i);
      if (!expectedColumnName.equalsIgnoreCase(StringUtils.trim(columnName))) {
        throw new ApiException(
            ErrorCode.INVALID_EXCEL_COLUMNS, "Column names should be ['이메일', '핸드폰', '이름']");
      }
    }
  }

  private boolean isRowEmpty(Row row) {
    if (row == null) return true;

    String email = getCellValueAsString(row.getCell(0));
    String phone = getCellValueAsString(row.getCell(1));

    return StringUtils.isBlank(email) && StringUtils.isBlank(phone);
  }

  private String getCellValueAsString(Cell cell) {
    if (cell == null) return null;
    switch (cell.getCellType()) {
      case STRING:
        return StringUtils.trim(cell.getStringCellValue());
      case NUMERIC:
        return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
      default:
        return "";
    }
  }
}
