package art.heredium.domain.membership.entity;

import art.heredium.domain.account.entity.Account;
import art.heredium.domain.common.entity.BaseEntity;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Table(name = "membership_mileage")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class MembershipMileage extends BaseEntity implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @Comment("이벤트 타입 (0:적립,1:승급,2:만료소멸,3:환불소멸)")
  @Column(name = "type", nullable = false)
  private Integer type;

  @Comment("대분류 (0:EXHIBITION,1:PROGRAM,2:COFFEE,3:ARTSHOP) - type=0 적립 시에만 사용")
  @Column(name = "category")
  private Integer category;

  @Comment("대분류 대상 ID (전시·프로그램·상품 등)")
  @Column(name = "category_id")
  private Long categoryId;

  @Comment("결제방법 (0:온라인,1:오프라인)")
  @Column(name = "payment_method", nullable = false)
  private Integer paymentMethod;

  @Comment("결제금액 (원단위)")
  @Column(name = "payment_amount", nullable = false)
  private Integer paymentAmount;

  @Comment("영문숫자 일련번호 / ARTSHOP 결제일시 등")
  @Column(name = "serial_number", length = 20)
  private String serialNumber;

  @Comment("마일리지 (-99~+99)")
  @Column(name = "mileage_amount", nullable = false)
  private Integer mileageAmount;

  @Comment("유효기간 만료일 (type=0 적립 시 계산)")
  @Column(name = "expiration_date")
  private LocalDateTime expirationDate;
}
