package art.heredium.domain.account.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.*;

import lombok.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.annotations.Comment;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.account.model.dto.request.PostAccountRequest;
import art.heredium.domain.account.model.dto.request.PostAccountSnsRequest;
import art.heredium.domain.account.model.dto.request.PutAdminAccountRequest;
import art.heredium.niceId.model.dto.response.PostNiceIdEncryptResponse;
import art.heredium.oauth.info.OAuth2UserInfo;
import art.heredium.oauth.provider.OAuth2Provider;

@Entity
@Getter
@Table(name = "account")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
// 회원
public class Account implements Serializable {
  private static final long serialVersionUID = -8931459553854696190L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @EqualsAndHashCode.Include
  private Long id;

  @Comment("이메일")
  @Column(name = "email", length = 255, unique = true)
  private String email;

  @Comment("비밀번호")
  @Column(name = "password", length = 255)
  private String password;

  @Comment("sns로그인 uuid")
  @Column(name = "sns_id", length = 255)
  private String snsId;

  @Comment("sns로그인 종류")
  @Column(name = "sns_type", length = 20, nullable = false)
  @Convert(converter = OAuth2Provider.Converter.class)
  private OAuth2Provider providerType;

  @Comment("생성일")
  @CreatedDate
  @Column(name = "created_date", nullable = false, updatable = false)
  private LocalDateTime createdDate;

  @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private AccountInfo accountInfo;

  @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  @ToString.Exclude
  private SleeperInfo sleeperInfo;

  public Account(PostAccountRequest dto, PostNiceIdEncryptResponse info, String encryptPassword) {
    validatePassword(dto.getPassword());
    this.email = dto.getEmail();
    this.password = encryptPassword;
    this.accountInfo = new AccountInfo(dto, info, this);
    this.providerType = OAuth2Provider.EMAIL;
  }

  public Account(
      OAuth2UserInfo userInfo,
      PostNiceIdEncryptResponse info,
      OAuth2Provider provider,
      Boolean isMarketingReceive) {
    this.email = userInfo.getEmail();
    this.providerType = provider;
    this.snsId = userInfo.getId();
    this.accountInfo = new AccountInfo(userInfo, info, isMarketingReceive, this);
  }

  public Account(
          PostAccountRequest dto,
      PostNiceIdEncryptResponse info,
      OAuth2UserInfo userInfo,
      OAuth2Provider provider) {
    this.email = userInfo.getEmail();
    this.providerType = provider;
    this.snsId = userInfo.getId();
    this.accountInfo = new AccountInfo(dto, info, this);
  }

  public void update(PutAdminAccountRequest dto) {
    this.email = dto.getEmail();
  }

  public void updatePassword(String rawPassword, String encryptPwd) {
    validatePassword(rawPassword);
    this.password = encryptPwd;
  }

  private void validatePassword(String password) {
    String pattern = "^.{8,16}$";
    if (!password.matches(pattern)) {
      throw new ApiException(ErrorCode.BAD_VALID, "8~16자리가 아님");
    }
  }

  public void setAccountInfo(AccountInfo accountInfo) {
    this.accountInfo = accountInfo;
  }

  public void setSleeperInfo(SleeperInfo sleeperInfo) {
    this.sleeperInfo = sleeperInfo;
  }

  public void terminate() {
    this.email = null;
    this.password = null;
    this.accountInfo = null;
    this.sleeperInfo = null;
    this.snsId = null;
    this.providerType = null;
  }

  public void updateEmail(String email) {
    this.email = email;
  }
}
