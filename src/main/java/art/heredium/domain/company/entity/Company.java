package art.heredium.domain.company.entity;

import javax.persistence.*;

import lombok.*;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.TypeDef;

@Entity
@Getter
@Setter
@Table(name = "company")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
// 회사
public class Company {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Comment("이름")
  @Column(name = "name", nullable = false, length = 100, unique = true)
  private String name;

  @Builder
  public Company(final String name) {
    this.name = name;
  }
}
