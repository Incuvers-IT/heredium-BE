package art.heredium.service;

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
import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.entity.PaymentStatus;
import art.heredium.domain.membership.entity.RegistrationType;
import art.heredium.domain.membership.repository.MembershipRegistrationRepository;
import art.heredium.domain.membership.repository.MembershipRepository;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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
  private final MembershipRepository membershipRepository;
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

  @Transactional
  public PostLoginResponse insert(HttpServletResponse response, PostAccountRequest dto) {

    // 1. 회원 기본 정보 저장
    if (isExistEmail(dto.getEmail())) {
      throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
    }
    PostNiceIdEncryptResponse info = niceIdService.decrypt(dto.getEncodeData());
    long age = ChronoUnit.YEARS.between(info.getBirthDate(), Constants.getNow());
    if (age < 14) {
      throw new ApiException(ErrorCode.UNDER_FOURTEEN);
    }
    Account entity = new Account(dto, info, bCryptPasswordEncoder.encode(dto.getPassword()));
    accountRepository.save(entity);

    // 2. 멤버십 등록
    // 1) 나이에 따라 code 결정 (19세 미만 → 학생(3), 그 외 → 기본(1))
    int targetCode = (age < 19) ? 3 : 1;

    // 2) code 로 멤버십 조회
    Membership membership = membershipRepository
            .findByCode(targetCode)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMBERSHIP_NOT_FOUND));

    // (3) MembershipRegistration 생성 및 저장
    this.membershipRegistrationRepository.save(
            new MembershipRegistration(
                    entity,
                    membership,
                    LocalDateTime.now(),
                    RegistrationType.MEMBERSHIP_PACKAGE,
                    PaymentStatus.COMPLETED,
                    "system",
                    "system"));

    // 3. 로그인
    PostLoginResponse res =
        authService.login(response, new PostLoginRequest(dto.getEmail(), dto.getPassword()), false);

    // 4) 메일·알림톡 발송은 예외 무시
    try {
      sendSignupNotifications(entity);
    } catch (Exception ex) {
      log.error("회원가입 메일/알림톡 발송 중 오류", ex);
    }

    return res;
  }

  private void sendSignupNotifications(Account entity) {
    Map<String, String> params = new HashMap<>();
    params.put("name", entity.getAccountInfo().getName());
    params.put("link", herediumProperties.getDomain());
    params.put("CSTel", herediumProperties.getTel());
    params.put("CSEmail", herediumProperties.getEmail());

    // 1. 메일 발송
    cloudMail.mail(entity.getEmail(), params, MailTemplate.SIGN_UP);

    // 2. 알림톡 발송 :  기존 회원가입 알림톡(템플릿 내용 추가)
    alimTalk.sendAlimTalk(entity.getAccountInfo().getPhone(), params, AlimTalkTemplate.SIGN_UP);

    // 3. 알림톡 발송 : D+7 마케팅 수신 동의를 통한 혜택 알림톡 단건 발송(안내문, 혜택) 회원가입일로부터 7일 이후
    // 대상 : 마케팅 비동의 대상
    // 발송 후 account_info - sms_request_id만 update
    // 예약 발송 삭제 : 발송 전 회원탈퇴 시, 발송 전 마케팅 동의한 회원
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

  public GetUserAccountInfoResponse updateByAccountInfo(PutUserAccountRequest dto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    Account entity = accountRepository.findById(userPrincipal.getId()).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
    MembershipRegistration membershipRegistration =
            membershipRegistrationRepository.findLatestForAccount(entity.getId()).orElse(null);

    entity.getAccountInfo().updateMarketing(dto);

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

  public Page<AccountWithMembershipInfoResponseV2> listWithMembershipInfoIncludingTitle(
      GetAccountWithMembershipInfoRequestV2 dto, Pageable pageable) {
    return accountRepository.searchWithMembershipInfoIncludingTitle(dto, pageable);
  }

  @Transactional(rollbackFor = Exception.class)
  public UploadCouponIssuanceTemplateResponse uploadCouponIssuance(MultipartFile file)
      throws IOException {
    List<CouponIssuanceUploadResponse> successCases = new ArrayList<>();
    List<String> failedCases = new ArrayList<>();
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
      String name = getCellValueAsString(row.getCell(1));
      String phone = getCellValueAsString(row.getCell(2));
      String identifier = this.getUniqueIdentifier(email, name, phone);

      if (identifier == null) {
        failedCases.add(
            String.format(
                "Email, phone or name is empty - Email: %s, Phone: %s, Name: %s",
                email, phone, name));
        continue;
      }

      // Skip duplicate entries without adding to failed cases
      if (!processedIdentifiers.add(identifier)) {
        continue;
      }

      final Account selectedAccount =
          accountRepository
              .findLatestLoginAccountByEmailAndPhoneAndName(email, phone, name)
              .orElse(null);

      if (selectedAccount == null) {
        failedCases.add(
            String.format(
                "No account found for Email:%s, Phone:%s and Name:%s", email, phone, name));
      } else {
        addAccountToList(selectedAccount, successCases);
      }
    }

    workbook.close();
    return new UploadCouponIssuanceTemplateResponse(successCases, failedCases);
  }

  private String getUniqueIdentifier(String email, String name, String phone) {
    if (StringUtils.isEmpty(email) || StringUtils.isEmpty(name) || StringUtils.isEmpty(phone)) {
      return null;
    }
    return String.format("email:%s, phone:%s, name:%s", email, phone, name);
  }

  private void addAccountToList(Account account, List<CouponIssuanceUploadResponse> accounts) {
    CouponIssuanceUploadResponse accountInfo =
        accountRepository.findAccountWithMembershipInfo(account);
    accounts.add(accountInfo);
  }

  private void validateHeaderRow(Row headerRow) {
    for (int i = 0; i <= 2; i++) {
      final String columnName = getCellValueAsString(headerRow.getCell(i));
      final String expectedColumnName = CouponIssuanceTemplateColumns.getColumnNameByIndex(i);
      if (!expectedColumnName.equalsIgnoreCase(StringUtils.trim(columnName))) {
        throw new ApiException(
            ErrorCode.INVALID_EXCEL_COLUMNS, "Column names should be ['계정', '이름', '연락처(숫자만 표기)']");
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
