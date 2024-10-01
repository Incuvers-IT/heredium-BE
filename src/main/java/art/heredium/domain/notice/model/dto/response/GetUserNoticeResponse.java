package art.heredium.domain.notice.model.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import art.heredium.domain.notice.entity.Notice;

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
