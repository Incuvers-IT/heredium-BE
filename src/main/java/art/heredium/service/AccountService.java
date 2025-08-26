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
import art.heredium.domain.coupon.entity.Coupon;
import art.heredium.domain.coupon.repository.CouponRepository;
import art.heredium.domain.coupon.repository.CouponUsageRepository;
import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.entity.PaymentStatus;
import art.heredium.domain.membership.entity.RegistrationType;
import art.heredium.domain.membership.repository.MembershipMileageRepository;
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
import art.heredium.oauth.info.OAuth2UserInfo;
import art.heredium.oauth.provider.OAuth2Provider;
import art.heredium.oauth.service.OAuthService;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
  private final MembershipMileageRepository membershipMileageRepository;
  private final CouponRepository couponRepository;
  private final CouponUsageService couponUsageService;
  private final CouponUsageRepository couponUsageRepository;
  private final OAuthService oAuthService;

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

    Account entity;
    PostNiceIdEncryptResponse info;

    // 1) 이메일 가입 vs 소셜 가입 분기
    if (dto.getSnsType() != null && dto.getSnsId() != null) {

      // (a) 토큰으로 OAuth2UserInfo 추출
      OAuth2Provider provider = OAuth2Provider.valueOf(dto.getSnsType().toUpperCase());
      OAuth2UserInfo userInfo = oAuthService.getUserInfo(provider, dto.getSnsId());

      // (b) 이미 등록된 SNS 사용자인지 체크
      Account account = accountRepository.findBySnsIdAndProviderType(userInfo.getId(), provider);
      if (account != null) {
        throw new ApiException(ErrorCode.ALREADY_EXIST_USERNAME);
      }

      // (c) 나이·본인인증 체크
      info = niceIdService.decrypt(dto.getEncodeData());

      long age = ChronoUnit.YEARS.between(info.getBirthDate(), Constants.getNow());
      if (age < 14) {
        throw new ApiException(ErrorCode.UNDER_FOURTEEN);
      }
      if (userInfo.getPhone() != null && !userInfo.getPhone().equals(info.getMobileNo())) {
        throw new ApiException(ErrorCode.NOT_EQ_PHONE);
      }

      entity = new Account(dto, info, userInfo, provider);

    }else{

      // 1. 회원 기본 정보 저장
      if (isExistEmail(dto.getEmail())) {
        throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
      }
      info = niceIdService.decrypt(dto.getEncodeData());
      long age = ChronoUnit.YEARS.between(info.getBirthDate(), Constants.getNow());
      if (age < 14) {
        throw new ApiException(ErrorCode.UNDER_FOURTEEN);
      }
      entity = new Account(dto, info, bCryptPasswordEncoder.encode(dto.getPassword()));
    }

    accountRepository.save(entity);

    // 2. 공통로직 - 여기서 마케팅 쿠폰 발급 조건 확인 및 발급
    boolean hasJob            = StringUtils.isNotBlank(dto.getJob());
    boolean hasState          = StringUtils.isNotBlank(dto.getState());
    boolean hasDistrict       = StringUtils.isNotBlank(dto.getDistrict());
    boolean hasAdditionalInfo = Boolean.TRUE.equals(dto.getAdditionalInfoAgreed());
    boolean hasMarketing      = Boolean.TRUE.equals(dto.getIsMarketingReceive());

    if (hasJob && hasState && hasDistrict && hasAdditionalInfo && hasMarketing) {
      // a) 마케팅 동의 혜택용 쿠폰 조회
      List<Coupon> coupons = couponRepository.findByMarketingConsentBenefitTrue();

      // b) 쿠폰 사용내역 생성/저장 (sendAlimtalk 여부는 false 로 설정)
      couponUsageService.distributeMembershipAndCompanyCoupons(
              entity,
              coupons,
              true,
              null,
              "SYSTEM"
      );
    }

    // 3. 멤버십 등록
    // 1) 나이에 따라 code 결정 (19세 미만 → 학생(3), 그 외 → 기본(1))
    long age = ChronoUnit.YEARS.between(info.getBirthDate(), Constants.getNow());
    int targetCode = (age < 19) ? 3 : 1;

    // 2) code 로 멤버십 조회
    Membership membership = membershipRepository
            .findByCode(targetCode)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMBERSHIP_NOT_FOUND));

    // 3) 만료일 계산 (19세 미만일 때만)
    LocalDateTime expirationDate = null;

    if (targetCode == 3) {
      // 생일 만 19세 되는 날 00:00 → 그 전날 23:59:59
      expirationDate = info.getBirthDate()
              .plusYears(19)
              .atStartOfDay()
              .minusSeconds(1);
    }

    // 4) MembershipRegistration 생성 및 저장
    MembershipRegistration registration =
            new MembershipRegistration(
                    entity,
                    membership,
                    LocalDateTime.now(),
                    RegistrationType.MEMBERSHIP_PACKAGE,
                    PaymentStatus.COMPLETED,
                    "SYSTEM",
                    "SYSTEM"
            );

    // 만료일 setter
    if (expirationDate != null) {
      registration.setExpirationDate(expirationDate);
    }

    membershipRegistrationRepository.save(registration);

    // 5) 쿠폰발급 (멤버십에 따른 쿠폰발급)
    couponUsageService.distributeCouponsForMembership(
            entity,           // Account
            registration,     // MembershipRegistration
            false,            // 알림톡 즉시 발송 여부
            null              // 알림톡 예약
    );

    // 5) 로그인 (공통)
    PostLoginResponse loginRes;

    if (dto.getSnsType() != null) {
      // 소셜 로그인
      OAuth2Provider provider = OAuth2Provider.valueOf(dto.getSnsType().toUpperCase());
      loginRes = oAuthService.loginByToken(response, provider, dto.getSnsId());
    } else {
      // 이메일 로그인
      loginRes = authService.login(response, new PostLoginRequest(dto.getEmail(), dto.getPassword()), false);
    }

    try {
      sendSignupNotifications(entity, registration);
    } catch (Exception ex) {
      log.error("회원가입 메일/알림톡 발송 중 오류", ex);
    }

    return loginRes;
  }

  private void sendSignupNotifications(Account entity, MembershipRegistration registration) {
    Map<String, String> params = new HashMap<>();
    params.put("name", entity.getAccountInfo().getName());
    params.put("membershipName", registration.getMembership().getName());
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

    // 첫 동의건에 대한 처리
    LocalDateTime prevAgreeDate = entity.getAccountInfo().getMarketingAgreedDate();

    // 마케팅 쿠폰 발급 조건 확인 및 발급
    boolean hasJob            = StringUtils.isNotBlank(dto.getJob());
    boolean hasState          = StringUtils.isNotBlank(dto.getState());
    boolean hasDistrict       = StringUtils.isNotBlank(dto.getDistrict());
    boolean hasAdditionalInfo = Boolean.TRUE.equals(dto.getAdditionalInfoAgreed());
    boolean hasMarketing      = Boolean.TRUE.equals(dto.getIsMarketingReceive());

    List<Coupon> coupons = Collections.emptyList();

    // dto.getMarketingAgreedDate() 정보가 없을 경우 첫 동의건만 쿠폰발급
    if (prevAgreeDate == null && hasJob && hasState && hasDistrict && hasAdditionalInfo && hasMarketing ) {
      // a) 마케팅 동의 혜택용 쿠폰 조회
      coupons = couponRepository.findByMarketingConsentBenefitTrue();

      // b) 쿠폰 사용내역 생성/저장 (sendAlimtalk 여부는 false 로 설정)
      couponUsageService.distributeMembershipAndCompanyCoupons(
              entity,
              coupons,
              true,
              null,
              "SYSTEM"
      );
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

    // 전화번호 변경 (encodeData 로직)
    if (dto.getEncodeData() != null) {
      PostNiceIdEncryptResponse info = niceIdService.decrypt(dto.getEncodeData());
      entity.getAccountInfo().updatePhone(info);
    }

    return new GetUserAccountInfoResponse(entity, membershipRegistration, coupons);
  }

  @Transactional
  public GetUserAccountInfoResponse updateByAccountInfo(PutUserAccountRequest dto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    Account entity = accountRepository.findById(userPrincipal.getId()).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

    String rawGender = dto.getGender();

    if (rawGender != null && !rawGender.isEmpty()) {
      char g = Character.toUpperCase(rawGender.charAt(0));  // 'M' 또는 'W'
      if (g == 'M' || g == 'W') {
        dto.setGender(String.valueOf(g));
      } else {
        // 예외 처리: 예상치 못한 값이 넘어오면 기본 'M' 으로 처리하거나 예외 던지기
        dto.setGender("M");
      }
    }

    entity.getAccountInfo().updatePhoneVerification(dto);

    // 테스트
    LocalDate birthDate = LocalDate.parse("2008-10-25");
//    LocalDate birthDate = LocalDate.parse(dto.getBirthDate());

    long age = ChronoUnit.YEARS.between(birthDate, Constants.getNow());

    if (age < 14) {
      throw new ApiException(ErrorCode.UNDER_FOURTEEN);
    }

    // 1) 나이에 따라 code 결정 (19세 미만 → 학생(3), 그 외 → 기본(1))
    int targetCode = (age < 19) ? 3 : 1;

    // 2) code 로 멤버십 조회
    Membership membership = membershipRepository
            .findByCode(targetCode)
            .orElseThrow(() -> new ApiException(ErrorCode.MEMBERSHIP_NOT_FOUND));

    // 3) 기존 registration 조회 (가장 최신)
    Optional<MembershipRegistration> optReg =
            membershipRegistrationRepository.findLatestForAccount(entity.getId());

    // 4) 만료일 계산 (학생 등급일 때만)
    LocalDateTime expirationDate = null;
    if (targetCode == 3) {
      // 만 19세 되는 날 00:00 에 minusSeconds(1) → 전날 23:59:59
      expirationDate = birthDate
              .plusYears(19)
              .atStartOfDay()
              .minusSeconds(1);

      membershipMileageRepository.softDeleteByAccountId(entity.getId());
    }

    MembershipRegistration reg;

    if (optReg.isPresent()) {
      // ── 업데이트 모드 ───────────────────────────
      reg = optReg.get();
      reg.setMembership(membership);
      reg.setRegistrationType(RegistrationType.MEMBERSHIP_PACKAGE);
      reg.setPaymentStatus(PaymentStatus.COMPLETED);
      reg.setRegistrationDate(LocalDateTime.now());
    } else {
      // ── 신규등록 모드 ───────────────────────────
      reg = new MembershipRegistration(
              entity,
              membership,
              LocalDateTime.now(),
              RegistrationType.MEMBERSHIP_PACKAGE,
              PaymentStatus.COMPLETED,
              "SYSTEM",
              "SYSTEM"
      );
    }

    reg.setExpirationDate(expirationDate);

    // 3) 저장 (JPA가 id 유무로 insert/update 결정)
    membershipRegistrationRepository.save(reg);

    // 5) 쿠폰발급 (멤버십에 따른 쿠폰발급)
    couponUsageService.distributeCouponsForMembership(
            entity,           // Account
            reg,              // MembershipRegistration
            false,            // 알림톡 즉시 발송 여부
            null              // 알림톡 예약
    );

    // 7) 멤버십 3(학생)으로 전환 시, 커밋 후 알림톡 발송
    if (targetCode == 3) {
      final String phone = entity.getAccountInfo().getPhone();
      final Map<String, String> params = new HashMap<>();
      params.put("name", entity.getAccountInfo().getName());
      params.put("membershipName", reg.getMembership().getName());

      // 트랜잭션 커밋 이후에만 전송
      if (TransactionSynchronizationManager.isSynchronizationActive()) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
          @Override public void afterCommit() {
            try {
              alimTalk.sendAlimTalk(phone, params, AlimTalkTemplate.MEMBERSHIP_TIER_DEMOTED);
            } catch (Exception e) {
              log.warn("멤버십 전환 알림톡 발송 실패: {}", e.getMessage(), e);
            }
          }
        });
      } else {
        // 트랜잭션 밖이면 즉시 실행
        try {
          alimTalk.sendAlimTalk(phone, params, AlimTalkTemplate.MEMBERSHIP_TIER_DEMOTED);
        } catch (Exception e) {
          log.warn("멤버십 전환 알림톡 발송 실패(즉시): {}", e.getMessage(), e);
        }
      }
    }

    return new GetUserAccountInfoResponse(entity, reg);
  }

  @Transactional
  public GetUserAccountInfoResponse updateByAccountIdentity(PutUserAccountRequest dto) {
    // 1) 로그인 사용자 조회
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    Account entity = accountRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

    // 2) 입력 정규화
    // 2-1) gender: 'M' / 'W'만 허용(소문자 등 들어오면 대문자로)
    String rawGender = dto.getGender();
    if (rawGender != null && !rawGender.isEmpty()) {
      char g = Character.toUpperCase(rawGender.charAt(0));
      dto.setGender((g == 'M' || g == 'W') ? String.valueOf(g) : "M");
    }

    // 2-2) phone: 숫자만 보존(예: 하이픈 제거)
    if (dto.getPhone() != null) {
      dto.setPhone(dto.getPhone().replaceAll("\\D", ""));
    }

    // 2-3) birthDate 파싱 (yyyy-MM-dd 또는 yyyyMMdd 허용)
    LocalDate birthDate = LocalDate.parse(dto.getBirthDate());

    // 3) 연령 검증(만 14세 미만 차단)
    long age = ChronoUnit.YEARS.between(birthDate, Constants.getNow());
    if (age < 14) {
      throw new ApiException(ErrorCode.UNDER_FOURTEEN);
    }

    // 4) "전화·성별·생년월일"만 업데이트
    //   - 기존에 사용하던 도메인 메서드가 있으면 그것을 사용하세요.
    entity.getAccountInfo().updatePhoneVerification(dto);

    // 6) 응답
    //    현재 멤버십 정보를 응답에 포함하고 싶다면 최신 등록만 조회해서 넣습니다.
    MembershipRegistration latestReg = membershipRegistrationRepository
            .findLatestForAccount(entity.getId())
            .orElse(null);

    long totalMileage = membershipMileageRepository.sumActiveMileageByAccount(entity.getId()); // "적립 마일리지: 점수(총합)"이면 이 값을 사용

    return new GetUserAccountInfoResponse(entity, latestReg, totalMileage);
  }

  @Transactional
  public GetUserAccountInfoResponse updateByMarketing(PutUserAccountRequest dto) {

    // 1) 로그인한 회원 조회
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    Account entity = accountRepository.findById(userPrincipal.getId()).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
    MembershipRegistration membershipRegistration =
            membershipRegistrationRepository.findLatestForAccount(entity.getId()).orElse(null);

    // 첫 동의건에 대한 처리
    LocalDateTime prevAgreeDate = entity.getAccountInfo().getMarketingAgreedDate();

    // 마케팅 쿠폰 발급 조건 확인 및 발급
    boolean hasJob            = StringUtils.isNotBlank(dto.getJob());
    boolean hasState          = StringUtils.isNotBlank(dto.getState());
    boolean hasDistrict       = StringUtils.isNotBlank(dto.getDistrict());
    boolean hasAdditionalInfo = Boolean.TRUE.equals(dto.getAdditionalInfoAgreed());
    boolean hasMarketing      = Boolean.TRUE.equals(dto.getIsMarketingReceive());

    List<Coupon> coupons = Collections.emptyList();

    // 마케팅 수신 동의한 경우에만 쿠폰 발급 처리
    // dto.getMarketingAgreedDate() 정보가 없을 경우 첫 동의건만 쿠폰발급
    if (prevAgreeDate == null && hasJob && hasState && hasDistrict && hasAdditionalInfo && hasMarketing ) {
      // a) 마케팅 동의 혜택용 쿠폰 조회
      coupons = couponRepository.findByMarketingConsentBenefitTrue();

      // b) 쿠폰 사용내역 생성/저장 (sendAlimtalk 여부는 false 로 설정)
      couponUsageService.distributeMembershipAndCompanyCoupons(
              entity,
              coupons,
              true,
              null,
              "SYSTEM"
      );
    }

    // 2) 마케팅 동의 정보 업데이트
    entity.getAccountInfo().updateMarketing(dto);

    return new GetUserAccountInfoResponse(entity, membershipRegistration, coupons);
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
    Account entity = accountRepository.findById(userPrincipal.getId())
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));

    Map<String, String> params = new HashMap<>();
    params.put("name", entity.getAccountInfo().getName());
    params.put("CSTel", herediumProperties.getTel());
    params.put("CSEmail", herediumProperties.getEmail());

    String email = entity.getEmail();
    String phone = entity.getAccountInfo().getPhone();
    entity.terminate();
    ticketRepository.terminateByAccount(entity.getId());

    // 1) 멤버십 관련 이력 소프트삭제
    int regCnt = membershipRegistrationRepository.softDeleteByAccountId(entity.getId());
    int mileCnt = membershipMileageRepository.softDeleteByAccountId(entity.getId());
    int couponCnt = couponUsageRepository.softDeleteByAccountId(entity.getId());

    // 2) 로깅
    log.info("Account {} terminated. softDeleted: registrations={}, mileages={}, couponUsages={}",
            entity.getId(), regCnt, mileCnt, couponCnt);

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
