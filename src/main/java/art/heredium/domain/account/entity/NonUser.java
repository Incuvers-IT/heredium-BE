package art.heredium.domain.account.entity;

import java.io.Serializable;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.annotations.Comment;

import art.heredium.domain.common.entity.BaseTimeEntity;
import art.heredium.domain.ticket.model.TicketHanaBankUserInfo;
import art.heredium.domain.ticket.model.dto.request.PostTicketHanaBankUserCommonRequest;
import art.heredium.domain.ticket.model.dto.request.PostTicketNonUserCommonRequest;
import art.heredium.niceId.model.dto.response.PostNiceIdEncryptResponse;

@Entity
@Getter
@Table(name = "non_user")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
// 비회원
public class NonUser extends BaseTimeEntity implements Serializable {
  private static final long serialVersionUID = 6439609847805351423L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Comment("이름")
  @Column(name = "name", length = 255)
  private String name;

  @Comment("이메일")
  @Column(name = "email", length = 255)
  private String email;

  @Comment("핸드폰 번호")
  @Column(name = "phone", length = 15)
  private String phone;

  @Comment("하나은행 여부 및 uuid, null일때는 비회원")
  @Column(name = "hana_bank_uuid", length = 255)
  private String hanaBankUuid;

  public NonUser(PostNiceIdEncryptResponse info, PostTicketNonUserCommonRequest dto) {
    this.name = info.getName();
    this.email = dto.getEmail();
    this.phone = info.getMobileNo();
    this.hanaBankUuid = null;
  }

  public NonUser(TicketHanaBankUserInfo info, PostTicketHanaBankUserCommonRequest dto) {
    this.name = info.getName();
    this.email = dto.getEmail();
    this.phone = dto.getPhone();
    this.hanaBankUuid = info.getHanaBankUuid();
  }

  public void update(
      TicketHanaBankUserInfo ticketHanaBankUserInfo, PostTicketHanaBankUserCommonRequest dto) {
    this.name = ticketHanaBankUserInfo.getName();
    this.email = dto.getEmail();
    this.phone = dto.getPhone();
    this.updateLastModifiedDate();
  }

  public void update(
      PostNiceIdEncryptResponse info,
      PostTicketNonUserCommonRequest postTicketNonUserCommonRequest) {
    this.name = info.getName();
    this.email = postTicketNonUserCommonRequest.getEmail();
    this.phone = info.getMobileNo();
    this.updateLastModifiedDate();
  }

  public boolean isHanaBank() {
    return this.hanaBankUuid != null;
  }

  public void terminate() {
    this.name = null;
    this.email = null;
    this.phone = null;
    this.hanaBankUuid = null;
  }
}
