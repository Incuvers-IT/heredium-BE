package art.heredium.domain.coffee.entity;

import art.heredium.domain.coffee.model.dto.request.PostAdminCoffeeRequest;
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
@Table(name = "coffee_price")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"coffee"})
//커피 가격
public class CoffeePrice implements Serializable {
    private static final long serialVersionUID = 3489094738347577328L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coffee_id", nullable = false)
    private Coffee coffee;

    @Comment("활성화 여부")
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled;

    @Comment("종류")
    @Column(name = "type", nullable = false)
    private String type;

    @Comment("가격")
    @Column(name = "price", nullable = false)
    private Long price;

    @OneToMany(mappedBy = "coffeePrice", cascade = CascadeType.ALL)
    private List<CoffeePriceDiscount> discounts = new ArrayList<>();

    public CoffeePrice(PostAdminCoffeeRequest.Price dto, Coffee coffee) {
        this.coffee = coffee;
        this.isEnabled = dto.getIsEnabled();
        this.type = dto.getType();
        this.price = dto.getPrice();
        this.discounts = dto.getDiscounts().stream().map(discount -> new CoffeePriceDiscount(discount, this)).collect(Collectors.toList());
    }

    public void update(PostAdminCoffeeRequest.Price dto) {
        this.isEnabled = dto.getIsEnabled();
        this.type = dto.getType();
        this.price = dto.getPrice();

        this.getDiscounts().removeIf(price -> dto.getDiscounts().stream().noneMatch(dtoPrice -> dtoPrice.getId() != null && dtoPrice.getId() == price.getId().intValue()));
        dto.getDiscounts().forEach(dtoPriceDiscounts -> {
            CoffeePriceDiscount up = this.getDiscounts().stream().filter(price -> dtoPriceDiscounts.getId() != null && price.getId().intValue() == dtoPriceDiscounts.getId()).findAny().orElse(null);
            if (up == null) {
                this.addDiscounts(new CoffeePriceDiscount(dtoPriceDiscounts, this));
            } else {
                up.update(dtoPriceDiscounts);
            }
        });
    }

    public void addDiscounts(CoffeePriceDiscount entity) {
        this.discounts.add(entity);
    }
}
