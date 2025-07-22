package art.heredium.domain.account.entity;

import art.heredium.core.util.Constants;
import art.heredium.domain.account.model.dto.request.PostAccountRequest;
import art.heredium.domain.account.model.dto.request.PostAccountSnsRequest;
import art.heredium.domain.account.model.dto.request.PutAdminAccountRequest;
import art.heredium.domain.account.model.dto.request.PutUserAccountRequest;
import art.heredium.domain.account.type.AuthType;
import art.heredium.niceId.model.dto.response.PostNiceIdEncryptResponse;
import art.heredium.oauth.info.OAuth2UserInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Table(name = "account_info")
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"account"})
// 회원정보
public class AccountInfo implements Serializable {
  private static final long serialVersionUID = 98625232369885267L;

  @Id
  @Column(name = "account_id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "account_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Account account;

  @Comment("이름")
  @Column(name = "name", nullable = false, length = 30)
  private String name;

  @Comment("핸드폰 번호")
  @Column(name = "phone", nullable = false, length = 15)
  private String phone;

  @Comment("권한")
  @Convert(converter = AuthType.Converter.class)
  @Column(name = "auth", nullable = false)
  private AuthType auth;

  @Comment("마지막 로그인 일시")
  @Column(name = "last_login_date")
  private LocalDateTime lastLoginDate;

  @Comment("지역가입자 여부")
  @Column(name = "is_local_resident", columnDefinition = "bit(1) DEFAULT false")
  private Boolean isLocalResident;

  @Comment("마케팅 수신 여부")
  @Column(
      name = "is_marketing_receive",
      nullable = false,
      columnDefinition = "bit(1) DEFAULT false")
  private Boolean isMarketingReceive;

  @Comment("성별 (M: 남성, W: 여성)")
  @Column(
          name = "gender",
          columnDefinition = "enum('M','W')")
  private String gender;

  @Comment("생년월일 (YYYY-MM-DD)")
  @Column(name = "birth_date")
  private String birthDate;

  @Comment("거주지 시/도 코드")
  @Column(name = "state", length = 30)
  private String state;

  @Comment("거주지 시/도 코드")
  @Column(name = "district", length = 30)
  private String district;

  @Comment("마케팅 동의 팝업 상태 (1: 대기, 0: 처리됨)")
  @Column(
          name = "marketing_pending",
          nullable = false,
          columnDefinition = "tinyint(1) default 1")
  private Boolean marketingPending;

  @Comment("마케팅 동의 일시")
  @Column(name = "marketing_agreed_date")
  private LocalDateTime marketingAgreedDate;

  @Comment("마케팅 수신동의 예약 알림톡 요청 id")
  @Type(type = "json")
  @Column(name = "sms_request_id", columnDefinition = "json")
  private List<String> smsRequestId;

  @Comment("직업 (자유 입력)")
  @Column(name = "job", length = 50)
  private String job;

  @Comment("추가 개인정보 수집 및 이용 동의 여부")
  @Column(name = "additional_info_agreed", nullable = false, columnDefinition = "tinyint(1) default 0")
  private Boolean additionalInfoAgreed;

  public AccountInfo(PostAccountRequest dto, PostNiceIdEncryptResponse info, Account account) {
    this.auth = AuthType.USER;
    this.name = info.getName();
    this.phone = info.getMobileNo();
    this.account = account;
    this.marketingPending   = dto.getMarketingPending();
    this.gender   = dto.getGender();
    this.birthDate   = dto.getBirthDate();

    // 5가지 항목이 모두 입력/동의되었는지 확인
    boolean hasJob            = StringUtils.isNotBlank(dto.getJob());
    boolean hasState          = StringUtils.isNotBlank(dto.getState());
    boolean hasDistrict       = StringUtils.isNotBlank(dto.getDistrict());
    boolean hasAdditionalInfo = Boolean.TRUE.equals(dto.getAdditionalInfoAgreed());
    boolean hasMarketing      = Boolean.TRUE.equals(dto.getIsMarketingReceive());

    boolean fullConsent = hasJob && hasState && hasDistrict && hasAdditionalInfo && hasMarketing;

    // (2) 전체 동의일 때만 값을 저장, 아니면 모두 기본값으로
    if (fullConsent) {
      this.job                    = dto.getJob();
      this.state                  = dto.getState();
      this.district               = dto.getDistrict();
      this.additionalInfoAgreed   = true;
      this.isMarketingReceive     = true;
      this.marketingAgreedDate    = Constants.getNow();
      this.isLocalResident   = dto.getIsLocalResident();
    } else {
      // 저장하지 않음(또는 false/null)
      this.job                    = null;
      this.state                  = null;
      this.district               = null;
      this.additionalInfoAgreed   = false;
      this.isMarketingReceive     = false;
      this.marketingAgreedDate    = null;
      this.isLocalResident        = false;
    }
  }

  public AccountInfo(PostAccountSnsRequest dto, PostNiceIdEncryptResponse info, Account account) {
    this.auth = AuthType.USER;
    this.name = info.getName();
    this.phone = info.getMobileNo();
    this.isLocalResident   = dto.getIsLocalResident();
    this.account = account;
    this.marketingPending   = dto.getMarketingPending();
    this.gender   = dto.getGender();
    this.birthDate   = dto.getBirthDate();
    this.state   = dto.getState();
    this.district   = dto.getDistrict();
    this.job = dto.getJob();
    this.additionalInfoAgreed = dto.getAdditionalInfoAgreed();

    // 마케팅 수신 동의 처리
    if (Boolean.TRUE.equals(dto.getIsMarketingReceive())) {
      // 동의 시
      this.isMarketingReceive = true;
      this.marketingAgreedDate = Constants.getNow();
    } else {
      // 동의 철회 시
      this.isMarketingReceive = false;
      this.marketingAgreedDate = null;
    }
  }

  public void update(PutAdminAccountRequest dto) {
    this.isLocalResident = dto.getIsLocalResident();
  }

  public void update(PutUserAccountRequest dto) {
    // 1) 동의 기반 필드: 모든 추가 정보가 채워지고 동의가 true일 때만 저장
    boolean hasJob          = StringUtils.isNotBlank(dto.getJob());
    boolean hasState        = StringUtils.isNotBlank(dto.getState());
    boolean hasDistrict     = StringUtils.isNotBlank(dto.getDistrict());
    boolean hasAdditional   = Boolean.TRUE.equals(dto.getAdditionalInfoAgreed());
    boolean hasMarketing    = Boolean.TRUE.equals(dto.getIsMarketingReceive());

    boolean fullConsent = hasJob && hasState && hasDistrict && hasAdditional && hasMarketing;

    if (fullConsent) {
      this.job                  = dto.getJob();
      this.state                = dto.getState();
      this.district             = dto.getDistrict();
      this.additionalInfoAgreed = true;
      this.isMarketingReceive   = true;
      this.marketingAgreedDate  = LocalDateTime.now();
      this.isLocalResident     = dto.getIsLocalResident();
    } else {
      // 하나라도 빠졌으면 모두 초기화
      this.job                  = null;
      this.state                = null;
      this.district             = null;
      this.additionalInfoAgreed = false;
      this.isMarketingReceive   = false;
      this.isLocalResident     = false;
    }
  }

  public void updateMarketing(PutUserAccountRequest dto) {
    this.job                = dto.getJob();
    this.state              = dto.getState();
    this.district           = dto.getDistrict();
    this.marketingPending   = dto.getMarketingPending();
    this.isLocalResident = dto.getIsLocalResident();
    this.additionalInfoAgreed = dto.getAdditionalInfoAgreed();

    // 마케팅 수신 동의 처리
    if (Boolean.TRUE.equals(dto.getIsMarketingReceive())) {
      // 동의 시
      this.isMarketingReceive = true;
      this.marketingAgreedDate = Constants.getNow();
    } else {
      // 동의 철회 시
      this.isMarketingReceive = false;
      this.marketingAgreedDate = null;
    }
  }

  public void updatePhoneVerification(PutUserAccountRequest dto) {
    this.phone             = dto.getPhone();
    this.gender             = dto.getGender();
    this.birthDate          = dto.getBirthDate();
  }

  public void updateLastLoginDate() {
    this.lastLoginDate = Constants.getNow();
  }

  public AccountInfo(SleeperInfo sleeperInfo, Account account) {
    this.auth = sleeperInfo.getAuth();
    this.name = sleeperInfo.getName();
    this.phone = sleeperInfo.getPhone();
    this.isLocalResident = sleeperInfo.getIsLocalResident();
    this.isMarketingReceive = sleeperInfo.getIsMarketingReceive();
    this.account = account;
    updateLastLoginDate();
  }

  public AccountInfo(
      OAuth2UserInfo userInfo,
      PostNiceIdEncryptResponse info,
      Boolean isMarketingReceive,
      Account account) {
    this.auth = AuthType.USER;
    this.name = info.getName();
    this.phone = info.getMobileNo();
    this.isLocalResident = false;
    this.marketingPending = false;
    this.isMarketingReceive = isMarketingReceive;
    this.account = account;
  }

  public void updatePhone(PostNiceIdEncryptResponse info) {
    this.name = info.getName();
    this.phone = info.getMobileNo();
  }

  public void updateLocalResident(Boolean isEnabled) {
    this.isLocalResident = isEnabled;
  }

  public void updateMarketingReceive(Boolean isEnabled) {
    this.isMarketingReceive = isEnabled;
  }
}
