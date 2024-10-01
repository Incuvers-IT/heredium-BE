package art.heredium.domain.policy.model.dto.response;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.policy.entity.Policy;

@Getter
@Setter
public class GetUserPolicyResponse {
  private Long id;
  private String title;

  public GetUserPolicyResponse(Policy entity) {
    this.id = entity.getId();
    this.title = entity.getTitle();
  }
}
