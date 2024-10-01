package art.heredium.domain.account.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import art.heredium.core.config.properties.HerediumProperties;
import art.heredium.core.util.Constants;
import art.heredium.domain.account.model.dto.request.PutAdminAccountRequest;
import art.heredium.domain.account.type.AuthType;

@Entity
@Getter
@Table(name = "sleeper_info")
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"account"})
// 휴면계정
public class SleeperInfo implements Serializable {
  private static final long serialVersionUID = 5481333308538945066L;

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
  @Column(name = "last_login_date", updatable = false)
  private LocalDateTime lastLoginDate;

  @Comment("휴면일시")
  @CreatedDate
  @Column(name = "sleep_date", nullable = false, updatable = false)
  private LocalDateTime sleepDate;

  @Comment("지역가입자 여부")
  @Column(name = "is_local_resident", nullable = false)
  private Boolean isLocalResident;

  @Comment("마케팅 수신 여부")
  @Column(name = "is_marketing_receive", nullable = false)
  private Boolean isMarketingReceive;

  public SleeperInfo(AccountInfo accountInfo) {
    this.auth = accountInfo.getAuth();
    this.name = accountInfo.getName();
    this.phone = accountInfo.getPhone();
    this.lastLoginDate = accountInfo.getLastLoginDate();
    this.isLocalResident = accountInfo.getIsLocalResident();
    this.isMarketingReceive = accountInfo.getIsMarketingReceive();
    this.account = accountInfo.getAccount();
  }

  public void update(PutAdminAccountRequest dto) {
    this.isLocalResident = dto.getIsLocalResident();
  }

  public void updateMarketingReceive(Boolean isEnabled) {
    this.isMarketingReceive = isEnabled;
  }

  public Map<String, String> getTerminateMailParam(
      HerediumProperties herediumProperties, String terminateDate) {
    Map<String, String> param = new HashMap<>();
    param.put("email", Constants.emailMasking(this.getAccount().getEmail()));
    param.put("name", this.getName());
    param.put("terminateDate", terminateDate);
    param.put("CSTel", herediumProperties.getTel());
    param.put("CSEmail", herediumProperties.getEmail());
    return param;
  }
}
