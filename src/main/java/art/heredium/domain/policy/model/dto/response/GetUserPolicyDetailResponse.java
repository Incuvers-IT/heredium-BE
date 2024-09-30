package art.heredium.domain.policy.model.dto.response;

import art.heredium.domain.policy.entity.Policy;
import art.heredium.domain.policy.type.PolicyType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetUserPolicyDetailResponse {
    private Long id;
    private String title;
    private LocalDateTime postDate;
    private String contents;
    private PolicyType type;

    public GetUserPolicyDetailResponse(Policy entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.postDate = entity.getPostDate();
        this.contents = entity.getContents();
        this.type = entity.getType();
    }
}
