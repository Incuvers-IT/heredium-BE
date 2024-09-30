package art.heredium.domain.program.entity;

import art.heredium.domain.program.model.dto.request.PostAdminProgramRequest;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Table(name = "program_price")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"program"})
//프로그램 가격
public class ProgramPrice implements Serializable {
    private static final long serialVersionUID = -6518737398641504354L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Comment("확성화 여부")
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @Comment("종류")
    @Column(name = "type", nullable = false)
    private String type;

    @Comment("가격")
    @Column(name = "price", nullable = false)
    private Long price;

    @OneToMany(mappedBy = "programPrice", cascade = CascadeType.ALL)
    private List<ProgramPriceDiscount> discounts = new ArrayList<>();

    public ProgramPrice(PostAdminProgramRequest.Price dto, Program program) {
        this.program = program;
        this.isEnabled = dto.getIsEnabled();
        this.type = dto.getType();
        this.price = dto.getPrice();
        this.discounts = dto.getDiscounts().stream().map(discount -> new ProgramPriceDiscount(discount, this)).collect(Collectors.toList());
    }

    public void update(PostAdminProgramRequest.Price dto) {
        this.isEnabled = dto.getIsEnabled();
        this.type = dto.getType();
        this.price = dto.getPrice();

        this.getDiscounts().removeIf(price -> dto.getDiscounts().stream().noneMatch(dtoPrice -> dtoPrice.getId() != null && dtoPrice.getId() == price.getId().intValue()));
        dto.getDiscounts().forEach(dtoPriceDiscounts -> {
            ProgramPriceDiscount up = this.getDiscounts().stream().filter(price -> dtoPriceDiscounts.getId() != null && price.getId().intValue() == dtoPriceDiscounts.getId()).findAny().orElse(null);
            if (up == null) {
                this.addDiscounts(new ProgramPriceDiscount(dtoPriceDiscounts, this));
            } else {
                up.update(dtoPriceDiscounts);
            }
        });
    }

    public void addDiscounts(ProgramPriceDiscount entity) {
        this.discounts.add(entity);
    }
}
