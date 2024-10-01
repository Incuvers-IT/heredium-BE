package art.heredium.domain.account.entity;

import java.io.Serializable;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.hibernate.annotations.Comment;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.account.model.dto.request.PostAdminRequest;
import art.heredium.domain.account.model.dto.request.PutAdminRequest;
import art.heredium.domain.log.entity.Log;
import art.heredium.domain.log.type.LogAction;
import art.heredium.domain.log.type.LogType;

@Entity
@Getter
@Table(name = "admin")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
// 관리자
public class Admin implements Serializable {
  private static final long serialVersionUID = -8931459553854696190L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Comment("이메일")
  @Column(name = "email", nullable = false, length = 255, unique = true)
  private String email;

  @Comment("비밀번호")
  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @OneToOne(mappedBy = "admin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private AdminInfo adminInfo;

  public Admin(PostAdminRequest dto, String encryptPassword) {
    validatePassword(dto.getPassword());
    this.email = dto.getEmail();
    this.password = encryptPassword;
    this.adminInfo = new AdminInfo(dto, this);
  }

  public void update(PutAdminRequest dto) {
    this.email = dto.getEmail();
    this.adminInfo.update(dto);
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

  public void setAdminInfo(AdminInfo adminInfo) {
    this.adminInfo = adminInfo;
  }

  public Log createInsertLog(Admin admin) {
    return new Log(admin, this.email, this.toString(), LogType.ADMIN, LogAction.INSERT);
  }

  public Log createUpdateLog(Admin admin) {
    return new Log(admin, this.email, this.toString(), LogType.ADMIN, LogAction.UPDATE);
  }

  public Log createDeleteLog(Admin admin) {
    return new Log(admin, this.email, this.toString(), LogType.ADMIN, LogAction.DELETE);
  }
}
