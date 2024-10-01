package art.heredium.domain.account.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import art.heredium.core.util.Constants;
import art.heredium.domain.account.model.dto.request.PostAccountRequest;
import art.heredium.domain.account.model.dto.request.PostAccountSnsRequest;
import art.heredium.domain.account.model.dto.request.PutAdminAccountRequest;
import art.heredium.domain.account.model.dto.request.PutUserAccountRequest;
import art.heredium.domain.account.type.AuthType;
import art.heredium.niceId.model.dto.response.PostNiceIdEncryptResponse;
import art.heredium.oauth.info.OAuth2UserInfo;

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

  public AccountInfo(PostAccountRequest dto, PostNiceIdEncryptResponse info, Account account) {
    this.auth = AuthType.USER;
    this.name = info.getName();
    this.phone = info.getMobileNo();
    this.isLocalResident = dto.getIsLocalResident();
    this.isMarketingReceive = dto.getIsMarketingReceive();
    this.account = account;
  }

  public AccountInfo(PostAccountSnsRequest dto, PostNiceIdEncryptResponse info, Account account) {
    this.auth = AuthType.USER;
    this.name = info.getName();
    this.phone = info.getMobileNo();
    this.isLocalResident = false;
    this.isMarketingReceive = dto.getIsMarketingReceive();
    this.account = account;
  }

  public void update(PutAdminAccountRequest dto) {
    this.isLocalResident = dto.getIsLocalResident();
  }

  public void update(PutUserAccountRequest dto) {
    this.isLocalResident = dto.getIsLocalResident();
    this.isMarketingReceive = dto.getIsMarketingReceive();
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
