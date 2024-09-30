package art.heredium.domain.exhibition.entity;

import art.heredium.domain.common.type.DiscountType;
import art.heredium.domain.exhibition.model.dto.request.PostAdminExhibitionRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Table(name = "exhibition_price_discount")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//전시 할인 가격
public class ExhibitionPriceDiscount implements Serializable {
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
    @JoinColumn(name = "exhibition_price_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ExhibitionPrice exhibitionPrice;

    public ExhibitionPriceDiscount(PostAdminExhibitionRequest.Price.PriceDiscount dto, ExhibitionPrice exhibitionPrice) {
        this.exhibitionPrice = exhibitionPrice;
        this.price = dto.getPrice();
        this.note = dto.getNote();
        this.type = dto.getType();
        this.enabled = dto.getEnabled();
    }

    public void update(PostAdminExhibitionRequest.Price.PriceDiscount dto) {
        this.price = dto.getPrice();
        this.note = dto.getNote();
        this.type = dto.getType();
        this.enabled = dto.getEnabled();
    }
}
