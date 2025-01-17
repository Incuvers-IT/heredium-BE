package art.heredium.domain.post.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.annotation.Id;

import org.hibernate.annotations.DynamicInsert;

import art.heredium.domain.common.entity.BaseEntity;

@Getter
@Setter
@Table(name = "post_history")
@DynamicInsert
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
@Builder
public class PostHistory extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "post_content", columnDefinition = "TEXT")
  private String postContent;

  @Column(name = "modify_user_email")
  private String modifyUserEmail;
}
