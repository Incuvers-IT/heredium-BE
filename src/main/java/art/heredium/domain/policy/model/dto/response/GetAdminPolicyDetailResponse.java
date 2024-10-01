package art.heredium.domain.policy.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.policy.entity.Policy;
import art.heredium.domain.policy.type.PolicyType;

@Getter
@Setter
public class GetAdminPolicyDetailResponse {
  private Long id;
  private String title;
  private LocalDateTime postDate;
  private String createdName;
  private LocalDateTime createdDate;
  private String lastModifiedName;
  private LocalDateTime lastModifiedDate;
  private Boolean isEnabled;
  private String contents;
  private PolicyType type;

  public GetAdminPolicyDetailResponse(Policy entity) {
    this.id = entity.getId();
    this.title = entity.getTitle();
    this.postDate = entity.getPostDate();
    this.createdName = entity.getCreatedName();
    this.createdDate = entity.getCreatedDate();
    this.lastModifiedName = entity.getLastModifiedName();
    this.lastModifiedDate = entity.getLastModifiedDate();
    this.isEnabled = entity.getIsEnabled();
    this.contents = entity.getContents();
    this.type = entity.getType();
  }
}
