package art.heredium.oauth.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import art.heredium.domain.membership.entity.Membership;
import art.heredium.domain.membership.entity.MembershipRegistration;
import art.heredium.domain.membership.entity.PaymentStatus;
import art.heredium.domain.membership.entity.RegistrationType;
import art.heredium.domain.membership.repository.MembershipRegistrationRepository;
import art.heredium.domain.membership.repository.MembershipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.config.properties.AppProperties;
import art.heredium.core.config.properties.HerediumProperties;
import art.heredium.core.jwt.AuthToken;
import art.heredium.core.jwt.AuthTokenProvider;
import art.heredium.core.jwt.JwtRedisUtil;
import art.heredium.core.util.Constants;
import art.heredium.core.util.CookieUtil;
import art.heredium.domain.account.entity.Account;
import art.heredium.domain.account.entity.UserPrincipal;
import art.heredium.domain.account.model.dto.request.PostAccountSnsRequest;
import art.heredium.domain.account.model.dto.response.PostLoginResponse;
import art.heredium.domain.account.repository.AccountRepository;
import art.heredium.ncloud.bean.CloudMail;
import art.heredium.ncloud.bean.HerediumAlimTalk;
import art.heredium.ncloud.type.AlimTalkTemplate;
import art.heredium.ncloud.type.MailTemplate;
import art.heredium.niceId.model.dto.response.PostNiceIdEncryptResponse;
import art.heredium.niceId.service.NiceIdService;
import art.heredium.oauth.info.KakaoUserTermsResponse;
import art.heredium.oauth.info.OAuth2UserInfo;
import art.heredium.oauth.properties.OAuth2Properties;
import art.heredium.oauth.provider.ClientRegistration;
import art.heredium.oauth.provider.OAuth2Provider;
import art.heredium.service.LoadUserService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class OAuthService {
  private final AuthTokenProvider authTokenProvider;
  private final LoadUserService loadUserService;
  private final AccountRepository accountRepository;
  private final AppProperties appProperties;
  private final OAuth2Properties oAuth2Properties;
  private final JwtRedisUtil jwtRedisUtil;
  private final CloudMail cloudMail;
  private final HerediumAlimTalk alimTalk;
  private final NiceIdService niceIdService;
  private final MembershipRepository membershipRepository;
  private final MembershipRegistrationRepository membershipRegistrationRepository;
  private final HerediumProperties herediumProperties;

  public String getToken(OAuth2Provider provider, String code) {
    ClientRegistration clientRegistration =
        oAuth2Properties.getRegistration().get(provider.name().toLowerCase());
    RestTemplate restTemplate = new RestTemplate();

    MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    parameters.add("client_id", clientRegistration.getClientId());
    parameters.add("client_secret", clientRegistration.getClientSecret());
    parameters.add("redirect_uri", clientRegistration.getRedirectUri());
    parameters.add("code", code);
    parameters.add("grant_type", clientRegistration.getAuthorizationGrantType());

    try {
      ResponseEntity<Map> responseEntity =
          restTemplate.postForEntity(clientRegistration.getTokenUri(), parameters, Map.class);
      if (responseEntity.getStatusCode() != HttpStatus.OK || responseEntity.getBody() == null) {
        throw new ApiException(ErrorCode.BAD_VALID);
      }
      return provider == OAuth2Provider.APPLE
          ? (String) responseEntity.getBody().get("id_token")
          : (String) responseEntity.getBody().get("access_token");
    } catch (Exception e) {
      throw new ApiException(ErrorCode.BAD_VALID);
    }
  }

  public PostLoginResponse loginByCode(
      HttpServletResponse response, OAuth2Provider provider, String code) {
    String token = getToken(provider, code);
    return loginByToken(response, provider, token);
  }

  public PostLoginResponse loginByToken(
      HttpServletResponse response, OAuth2Provider provider, String token) {
    OAuth2UserInfo userInfo = getUserInfo(provider, token);
    Account account = accountRepository.findBySnsIdAndProviderType(userInfo.getId(), provider);
    if (account == null) {
      throw new ApiException(ErrorCode.USER_NOT_FOUND, token);
    }
    UserPrincipal userPrincipal = (UserPrincipal) loadUserService.loadUser(account.getId());

    Date now = new Date();
    String userId = userPrincipal.getId().toString();
    AuthToken accessToken =
        authTokenProvider.createAuthToken(
            userId,
            userPrincipal.getAuth(),
            new Date(now.getTime() + appProperties.getAuth().getAccessTokenExpiry().toMillis()));
    AuthToken refreshToken =
        authTokenProvider.createAuthToken(
            userId,
            userPrincipal.getAuth(),
            new Date(now.getTime() + appProperties.getAuth().getRefreshTokenExpiry().toMillis()));
    jwtRedisUtil.setDataExpire(
        refreshToken.getToken(),
        userId,
        appProperties.getAuth().getRefreshTokenExpiry().getSeconds());

    int cookieMaxAge = (int) appProperties.getAuth().getRefreshTokenExpiry().toMinutes() * 60;
    String refreshTokenName = appProperties.getAuth().getRefreshTokenName();
    CookieUtil.addCookie(response, refreshTokenName, refreshToken.getToken(), cookieMaxAge);
    updateLoginDate(userId);
    return new PostLoginResponse(
        accessToken.getToken(),
        userPrincipal.getIsSleeper(),
        userPrincipal.getIsSleeper() ? userPrincipal.getName() : null);
  }

  @Transactional
  public PostLoginResponse insert(
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2Provider provider,
      PostAccountSnsRequest dto) {

    // 1. 회원 기본 정보 저장
//    OAuth2UserInfo userInfo = getUserInfo(provider, dto.getToken());
//    PostNiceIdEncryptResponse info = niceIdService.decrypt(dto.getEncodeData());
//    Account account = accountRepository.findBySnsIdAndProviderType(userInfo.getId(), provider);
//    if (account != null) {
//      throw new ApiException(ErrorCode.ALREADY_EXIST_USERNAME);
//    }
//    Account entity;
//    if (provider == OAuth2Provider.KAKAO) {
//      boolean isAllow;
//      try {
//        KakaoUserTermsResponse kaKaoUserTerms = getKaKaoUserTerms(provider, dto.getToken());
//        isAllow =
//            kaKaoUserTerms.getAllowed_service_terms().stream()
//                .anyMatch(x -> x.getTag().equals("marketing"));
//      } catch (Exception e) {
//        isAllow = false;
//      }
//      entity = new Account(userInfo, info, provider, isAllow);
//    } else if (provider == OAuth2Provider.NAVER) {
//      entity = new Account(userInfo, info, provider, dto.getIsMarketingReceive());
//    } else {
//      entity = new Account(dto, info, userInfo, provider);
//    }
//    entity = new Account(dto, info, userInfo, provider);
//
//    long age = ChronoUnit.YEARS.between(info.getBirthDate(), Constants.getNow());
//    if (age < 14) {
//      throw new ApiException(ErrorCode.UNDER_FOURTEEN);
//    }
//    if (userInfo.getPhone() != null && !userInfo.getPhone().equals(info.getMobileNo())) {
//      throw new ApiException(ErrorCode.NOT_EQ_PHONE);
//    }
//
//    accountRepository.save(entity);

    // 2. 멤버십 등록
    // 1) 나이에 따라 code 결정 (19세 미만 → 학생(3), 그 외 → 기본(1))
//    int targetCode = (age < 19) ? 3 : 1;

    // 2) code 로 멤버십 조회
//    Membership membership = membershipRepository
//            .findByCode(targetCode)
//            .orElseThrow(() -> new ApiException(ErrorCode.MEMBERSHIP_NOT_FOUND));

    // (3) MembershipRegistration 생성 및 저장
//    this.membershipRegistrationRepository.save(
//            new MembershipRegistration(
//                    entity,
//                    membership,
//                    LocalDateTime.now(),
//                    RegistrationType.MEMBERSHIP_PACKAGE,
//                    PaymentStatus.COMPLETED,
//                    "system",
//                    "system"));

    // 3. 로그인
//    PostLoginResponse res = loginByToken(response, provider, dto.getToken());

    // 4) 메일·알림톡 발송은 예외 무시
//    try {
//      sendSignupNotifications(entity);
//    } catch (Exception ex) {
//      log.error("회원가입 메일/알림톡 발송 중 오류", ex);
//    }

//    return res;
    return null;
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

    // 2. 알림톡 발송 : D+7 마케팅 수신 동의를 통한 혜택 알림톡 단건 발송(안내문, 혜택) 회원가입일로부터 7일 이후
    // 대상 : 마케팅 비동의 대상
    // 발송 후 account_info - sms_request_id만 update
    // 예약 발송 삭제 : 발송 전 회원탈퇴 시, 발송 전 마케팅 동의한 회원
  }

  private void updateLoginDate(String userId) {
    accountRepository
        .findById(Long.valueOf(userId))
        .ifPresent(
            account -> {
              if (account.getAccountInfo() != null) {
                account.getAccountInfo().updateLastLoginDate();
                accountRepository.save(account);
              }
            });
  }

  public OAuth2UserInfo getUserInfo(OAuth2Provider provider, String token) {

    if (provider == OAuth2Provider.APPLE) {
      Map<String, Object> userId =
          provider.getUserInfoFromJwt(oAuth2Properties.getRegistration(), token);
      return provider.getUserInfo(userId);
    } else {
      String userInfoUri =
          provider
              .getClientRegistration(
                  oAuth2Properties.getRegistration().get(provider.name().toLowerCase()))
              .getUserInfoUri();
      Map map = requestRestTemplate(HttpMethod.POST, userInfoUri, token, null, Map.class);
      return provider.getUserInfo(map);
    }
  }

  private <T> T requestRestTemplate(
      HttpMethod method,
      String url,
      String token,
      MultiValueMap<String, String> map,
      Class<T> cls) {
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    requestHeaders.add("Authorization", "Bearer " + token);

    HttpEntity<HttpHeaders> requestEntity = new HttpEntity<>(requestHeaders);

    try {
      ResponseEntity<T> responseEntity =
          map == null
              ? restTemplate.exchange(url, method, requestEntity, cls)
              : restTemplate.exchange(url, method, requestEntity, cls, map);
      if (responseEntity.getStatusCode() != HttpStatus.OK || responseEntity.getBody() == null) {
        throw new ApiException(ErrorCode.BAD_VALID);
      }
      return responseEntity.getBody();
    } catch (Exception e) {
      e.printStackTrace();
      throw new ApiException(ErrorCode.BAD_VALID);
    }
  }

  public KakaoUserTermsResponse getKaKaoUserTerms(OAuth2Provider provider, String token) {
    if (provider != OAuth2Provider.KAKAO) {
      return null;
    }
    String userTermsUri =
        provider
            .getClientRegistration(
                oAuth2Properties.getRegistration().get(provider.name().toLowerCase()))
            .getUserTermsUri();
    return requestRestTemplate(
        HttpMethod.GET, userTermsUri, token, null, KakaoUserTermsResponse.class);
  }
}
