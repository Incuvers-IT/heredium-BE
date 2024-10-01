package art.heredium.domain.account.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

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

import art.heredium.core.util.Constants;
import art.heredium.domain.account.model.dto.request.PostAdminRequest;
import art.heredium.domain.account.model.dto.request.PutAdminRequest;
import art.heredium.domain.account.type.AuthType;

@Entity
@Getter
@Table(name = "admin_info")
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = {"admin"})
// 관리자 정보
public class AdminInfo implements Serializable {
  private static final long serialVersionUID = 98625232369885267L;

  @Id
  @Column(name = "admin_id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "admin_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Admin admin;

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

  @Comment("생성일")
  @CreatedDate
  @Column(name = "created_date", nullable = false, updatable = false)
  private LocalDateTime createdDate;

  @Column(nullable = false, columnDefinition = "bit(1) DEFAULT true")
  private Boolean isEnabled;

  public AdminInfo(PostAdminRequest dto, Admin admin) {
    this.auth = dto.getAuth();
    this.name = dto.getName();
    this.phone = dto.getPhone();
    this.isEnabled = true;
    this.admin = admin;
  }

  public void update(PutAdminRequest dto) {
    this.auth = dto.getAuth();
    this.name = dto.getName();
    this.phone = dto.getPhone();
    this.isEnabled = dto.getIsEnabled();
  }

  public void updateLastLoginDate() {
    this.lastLoginDate = Constants.getNow();
  }
}
