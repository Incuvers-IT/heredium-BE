package art.heredium.domain.program.entity;

import java.io.Serializable;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import org.hibernate.annotations.Comment;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import art.heredium.domain.common.type.DiscountType;
import art.heredium.domain.program.model.dto.request.PostAdminProgramRequest;

@Entity
@Getter
@Table(name = "program_price_discount")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 프로그램 할인 가격
public class ProgramPriceDiscount implements Serializable {
  private static final long serialVersionUID = 6904362583681763883L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Comment("가격")
  @Column(name = "price", nullable = false)
  private Long price;

  @Comment("비고")
  @Column(name = "note", length = 255, nullable = false)
  private String note;

  @Comment("종류")
  @Convert(converter = DiscountType.Converter.class)
  @Column(name = "type", nullable = false)
  private DiscountType type;

  @Comment("활성화 여부")
  @Column(name = "enabled", nullable = false)
  private Boolean enabled;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "program_price_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ProgramPrice programPrice;

  public ProgramPriceDiscount(
      PostAdminProgramRequest.Price.PriceDiscount dto, ProgramPrice programPrice) {
    this.programPrice = programPrice;
    this.price = dto.getPrice();
    this.note = dto.getNote();
    this.type = dto.getType();
    this.enabled = dto.getEnabled();
  }

  public void update(PostAdminProgramRequest.Price.PriceDiscount dto) {
    this.price = dto.getPrice();
    this.note = dto.getNote();
    this.type = dto.getType();
    this.enabled = dto.getEnabled();
  }
}
