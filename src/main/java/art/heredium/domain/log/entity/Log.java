package art.heredium.domain.log.entity;

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

import art.heredium.core.util.Constants;
import art.heredium.domain.account.entity.Admin;
import art.heredium.domain.log.type.LogAction;
import art.heredium.domain.log.type.LogType;

@Entity
@Getter
@Table
@DynamicInsert
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"admin"})
// 로그
public class Log implements Serializable {
  private static final long serialVersionUID = 4943948295202974382L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Comment("동작")
  @Convert(converter = LogAction.Converter.class)
  @Column(name = "action", nullable = false, updatable = false)
  private LogAction action;

  @Comment("종류")
  @Convert(converter = LogType.Converter.class)
  @Column(name = "type", nullable = false, updatable = false)
  private LogType type;

  @Comment("제목")
  @Column(
      name = "title",
      nullable = false,
      length = 100,
      updatable = false,
      columnDefinition = "VARCHAR(100) DEFAULT ''")
  private String title;

  @Comment("내용")
  @Column(name = "contents", columnDefinition = "LONGTEXT", updatable = false)
  private String contents;

  @Comment("ip")
  @Column(name = "ip", nullable = false, length = 40, updatable = false)
  private String ip;

  @Comment("이메일")
  @Column(name = "email", nullable = false, length = 255, updatable = false)
  private String email;

  @Comment("이름")
  @Column(name = "name", nullable = false, length = 30, updatable = false)
  private String name;

  @Comment("생성일")
  @CreatedDate
  @Column(name = "created_date", nullable = false, updatable = false)
  private LocalDateTime createdDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "admin_id")
  private Admin admin;

  public Log(Admin admin, String title, String contents, LogType type, LogAction action) {
    this.admin = admin;
    this.email = admin.getEmail();
    this.name = admin.getAdminInfo().getName();
    this.type = type;
    this.action = action;
    this.title = title;
    this.contents = contents.toString();
    this.ip = Constants.getIP();
  }
}
