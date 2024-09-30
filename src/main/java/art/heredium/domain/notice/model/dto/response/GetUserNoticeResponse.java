package art.heredium.domain.notice.model.dto.response;

import art.heredium.domain.notice.entity.Notice;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetUserNoticeResponse {
    private Long id;
    private String title;
    private Boolean isNotice;
    private LocalDateTime postDate;

    public GetUserNoticeResponse(Notice entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.isNotice = entity.getIsNotice();
        this.postDate = entity.getPostDate();
    }
}
