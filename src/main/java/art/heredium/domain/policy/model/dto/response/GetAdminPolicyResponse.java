package art.heredium.domain.policy.model.dto.response;

import art.heredium.domain.policy.entity.Policy;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetAdminPolicyResponse {
    private Long id;
    private Boolean isProgress;
    private String title;
    private LocalDateTime postDate;
    private String createdName;
    private LocalDateTime createdDate;
    private String lastModifiedName;
    private LocalDateTime lastModifiedDate;
    private Boolean isEnabled;

    public GetAdminPolicyResponse(Policy entity, Long postingId) {
        this.id = entity.getId();
        this.isProgress = postingId != null && entity.getId().longValue() == postingId;
        this.title = entity.getTitle();
        this.postDate = entity.getPostDate();
        this.createdName = entity.getCreatedName();
        this.createdDate = entity.getCreatedDate();
        this.lastModifiedName = entity.getLastModifiedName();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.isEnabled = entity.getIsEnabled();
    }
}
