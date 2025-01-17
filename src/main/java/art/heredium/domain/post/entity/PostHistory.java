package art.heredium.domain.post.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.annotation.Id;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import art.heredium.domain.common.entity.BaseEntity;

@Getter
@Setter
@Table(name = "post_history")
@DynamicInsert
@TypeDef(name = "json", typeClass = JsonStringType.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
public class PostHistory extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Type(type = "json")
  @Column(name = "post_content", columnDefinition = "TEXT")
  private String postContent;

  @Column(name = "modify_email")
  private String modifyEmail;
}
