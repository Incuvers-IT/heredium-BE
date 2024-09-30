package art.heredium.domain.coffee.entity;

import art.heredium.domain.coffee.model.dto.request.PostAdminCoffeeRequest;
import art.heredium.domain.common.model.Storage;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Table(name = "coffee_writer_info")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"coffeeWriter"})
//커피 인원 정보
public class CoffeeWriterInfo implements Serializable {
    private static final long serialVersionUID = 4922467054075409408L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coffee_writer_id", nullable = false)
    private CoffeeWriter coffeeWriter;

    @Comment("썸네일")
    @Type(type = "json")
    @Column(name = "thumbnail", columnDefinition = "json")
    private Storage thumbnail;

    @Comment("이름")
    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Comment("소개")
    @Column(name = "intro", nullable = false, columnDefinition = "TEXT")
    private String intro;

    @Comment("순서")
    @Column(name = "orders", nullable = false)
    private Long order;

    public CoffeeWriterInfo(PostAdminCoffeeRequest.Writer.WriterInfo dto, CoffeeWriter coffeeWriter, long order) {
        this.coffeeWriter = coffeeWriter;
        this.thumbnail = dto.getThumbnail();
        this.name = dto.getName();
        this.intro = dto.getIntro();
        this.order = order;
    }

    public void update(PostAdminCoffeeRequest.Writer.WriterInfo dto, long order) {
        this.thumbnail = dto.getThumbnail();
        this.name = dto.getName();
        this.intro = dto.getIntro();
        this.order = order;
    }
}
