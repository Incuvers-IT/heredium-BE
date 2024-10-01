package art.heredium.domain.exhibition.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

import art.heredium.domain.exhibition.model.dto.request.PostAdminExhibitionRequest;

@Entity
@Getter
@Table(name = "exhibition_price")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"exhibition"})
// 전시 가격
public class ExhibitionPrice implements Serializable {
  private static final long serialVersionUID = 3489094738347577328L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exhibition_id", nullable = false)
  private Exhibition exhibition;

  @Comment("확성화 여부")
  @Column(name = "is_enabled", nullable = false)
  private Boolean isEnabled;

  @Comment("종류")
  @Column(name = "type", nullable = false)
  private String type;

  @Comment("가격")
  @Column(name = "price", nullable = false)
  private Long price;

  @OneToMany(mappedBy = "exhibitionPrice", cascade = CascadeType.ALL)
  private List<ExhibitionPriceDiscount> discounts = new ArrayList<>();

  public ExhibitionPrice(PostAdminExhibitionRequest.Price dto, Exhibition exhibition) {
    this.exhibition = exhibition;
    this.isEnabled = dto.getIsEnabled();
    this.type = dto.getType();
    this.price = dto.getPrice();
    this.discounts =
        dto.getDiscounts().stream()
            .map(discount -> new ExhibitionPriceDiscount(discount, this))
            .collect(Collectors.toList());
  }

  public void update(PostAdminExhibitionRequest.Price dto) {
    this.isEnabled = dto.getIsEnabled();
    this.type = dto.getType();
    this.price = dto.getPrice();

    this.getDiscounts()
        .removeIf(
            price ->
                dto.getDiscounts().stream()
                    .noneMatch(
                        dtoPrice ->
                            dtoPrice.getId() != null
                                && dtoPrice.getId() == price.getId().intValue()));
    dto.getDiscounts()
        .forEach(
            dtoPriceDiscounts -> {
              ExhibitionPriceDiscount up =
                  this.getDiscounts().stream()
                      .filter(
                          price ->
                              dtoPriceDiscounts.getId() != null
                                  && price.getId().intValue() == dtoPriceDiscounts.getId())
                      .findAny()
                      .orElse(null);
              if (up == null) {
                this.addDiscounts(new ExhibitionPriceDiscount(dtoPriceDiscounts, this));
              } else {
                up.update(dtoPriceDiscounts);
              }
            });
  }

  public void addDiscounts(ExhibitionPriceDiscount entity) {
    this.discounts.add(entity);
  }
}
